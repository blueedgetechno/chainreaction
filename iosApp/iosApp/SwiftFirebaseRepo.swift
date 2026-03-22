import Foundation
import FirebaseAuth
import FirebaseDatabase
import shared

/// Swift implementation of the Kotlin FirebaseBridge protocol.
/// Uses Firebase iOS SDK for Auth and Realtime Database.
class SwiftFirebaseRepo: NSObject, FirebaseBridge {

    private let db: Database
    private let roomsRef: DatabaseReference
    private let auth = Auth.auth()
    private var listeners: [String: DatabaseHandle] = [:]

    override init() {
        db = Database.database(url: "https://chainreaction-8e4ec-default-rtdb.asia-southeast1.firebasedatabase.app/")
        roomsRef = db.reference(withPath: "rooms")
        super.init()
    }

    // MARK: - Auth

    func ensureAuth(onSuccess: @escaping (String) -> Void, onError: @escaping (String) -> Void) {
        if let uid = auth.currentUser?.uid {
            onSuccess(uid)
            return
        }
        auth.signInAnonymously { result, error in
            if let uid = result?.user.uid {
                onSuccess(uid)
            } else {
                onError(error?.localizedDescription ?? "Auth failed")
            }
        }
    }

    func getUid() -> String {
        return auth.currentUser?.uid ?? ""
    }

    // MARK: - Room Code

    private func generateRoomCode() -> String {
        let chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return String((0..<6).map { _ in chars.randomElement()! })
    }

    // MARK: - Create Room

    func createRoom(
        gridSize: Int32, gameVariant: String, hostName: String, hostColorIndex: Int32,
        onSuccess: @escaping (String) -> Void, onError: @escaping (String) -> Void
    ) {
        ensureAuth(onSuccess: { [weak self] uid in
            guard let self = self else { return }
            let roomCode = self.generateRoomCode()

            let room: [String: Any] = [
                "hostUid": uid,
                "guestUid": "",
                "status": "WAITING",
                "gridSize": Int(gridSize),
                "gameVariant": gameVariant,
                "hostName": hostName,
                "hostColorIndex": Int(hostColorIndex),
                "guestName": "",
                "guestColorIndex": 1,
                "currentPlayerId": 1,
                "moveCount": 0,
                "winnerId": 0,
                "gameStatus": "IN_PROGRESS",
                "lastMoveRow": -1,
                "lastMoveCol": -1,
                "lastMoveBy": 0,
                "lastMoveTimestamp": 0,
                "board": [] as [String],
                "createdAt": ServerValue.timestamp()
            ]

            self.roomsRef.child(roomCode).setValue(room) { error, _ in
                if let error = error {
                    onError(error.localizedDescription)
                } else {
                    // Set onDisconnect to mark room finished if host leaves
                    self.roomsRef.child(roomCode).child("status")
                        .onDisconnectSetValue("FINISHED")
                    onSuccess(roomCode)
                }
            }
        }, onError: onError)
    }

    // MARK: - Join Room

    func joinRoom(
        roomCode: String, guestName: String, guestColorIndex: Int32,
        onSuccess: @escaping (KotlinBoolean) -> Void, onError: @escaping (String) -> Void
    ) {
        ensureAuth(onSuccess: { [weak self] uid in
            guard let self = self else { return }
            let roomRef = self.roomsRef.child(roomCode)

            roomRef.getData { error, snapshot in
                guard let snapshot = snapshot, snapshot.exists() else {
                    onSuccess(KotlinBoolean(value: false))
                    return
                }

                let status = snapshot.childSnapshot(forPath: "status").value as? String ?? ""
                if status != "WAITING" {
                    onSuccess(KotlinBoolean(value: false))
                    return
                }

                let hostUid = snapshot.childSnapshot(forPath: "hostUid").value as? String ?? ""
                if hostUid == uid {
                    onSuccess(KotlinBoolean(value: false))
                    return
                }

                let hostColor = snapshot.childSnapshot(forPath: "hostColorIndex").value as? Int ?? 0
                let actualGuestColor = Int(guestColorIndex) == hostColor
                    ? (Int(guestColorIndex) + 1) % 8
                    : Int(guestColorIndex)

                let updates: [String: Any] = [
                    "guestUid": uid,
                    "guestName": guestName,
                    "guestColorIndex": actualGuestColor,
                    "status": "IN_PROGRESS"
                ]

                roomRef.updateChildValues(updates) { error, _ in
                    if let error = error {
                        onError(error.localizedDescription)
                    } else {
                        roomRef.child("status").cancelDisconnectOperations()
                        roomRef.child("guestUid").onDisconnectSetValue("disconnected")
                        onSuccess(KotlinBoolean(value: true))
                    }
                }
            }
        }, onError: onError)
    }

    // MARK: - Random Matchmaking

    func findRandomMatch(
        gridSize: Int32, gameVariant: String, playerName: String, playerColorIndex: Int32,
        onSuccess: @escaping (String) -> Void, onError: @escaping (String) -> Void
    ) {
        ensureAuth(onSuccess: { [weak self] uid in
            guard let self = self else { return }

            // Look for existing waiting rooms
            self.roomsRef
                .queryOrdered(byChild: "status")
                .queryEqual(toValue: "WAITING")
                .getData { error, snapshot in
                    guard let snapshot = snapshot else {
                        // No rooms found, create one
                        self.createRoom(
                            gridSize: gridSize, gameVariant: gameVariant,
                            hostName: playerName, hostColorIndex: playerColorIndex,
                            onSuccess: onSuccess, onError: onError
                        )
                        return
                    }

                    // Try to join an existing room
                    var joined = false
                    let group = DispatchGroup()

                    for child in snapshot.children.allObjects as? [DataSnapshot] ?? [] {
                        if joined { break }
                        let hostUid = child.childSnapshot(forPath: "hostUid").value as? String ?? ""
                        if hostUid == uid { continue }
                        guard let code = child.key as String? else { continue }

                        group.enter()
                        self.joinRoom(
                            roomCode: code, guestName: playerName,
                            guestColorIndex: playerColorIndex,
                            onSuccess: { success in
                                if success.boolValue && !joined {
                                    joined = true
                                    onSuccess(code)
                                }
                                group.leave()
                            },
                            onError: { _ in group.leave() }
                        )

                        if joined { break }
                    }

                    group.notify(queue: .main) {
                        if !joined {
                            self.createRoom(
                                gridSize: gridSize, gameVariant: gameVariant,
                                hostName: playerName, hostColorIndex: playerColorIndex,
                                onSuccess: onSuccess, onError: onError
                            )
                        }
                    }
                }
        }, onError: onError)
    }

    // MARK: - Send Move

    func sendMove(
        roomCode: String, row: Int32, col: Int32, playerId: Int32,
        onSuccess: @escaping () -> Void, onError: @escaping (String) -> Void
    ) {
        let updates: [String: Any] = [
            "lastMoveRow": Int(row),
            "lastMoveCol": Int(col),
            "lastMoveBy": Int(playerId),
            "lastMoveTimestamp": ServerValue.timestamp()
        ]
        roomsRef.child(roomCode).updateChildValues(updates) { error, _ in
            if let error = error {
                onError(error.localizedDescription)
            } else {
                onSuccess()
            }
        }
    }

    // MARK: - Sync Game State

    func syncGameState(
        roomCode: String, board: [String],
        currentPlayerId: Int32, moveCount: Int32,
        winnerId: Int32, gameStatus: String,
        onSuccess: @escaping () -> Void, onError: @escaping (String) -> Void
    ) {
        let updates: [String: Any] = [
            "board": board,
            "currentPlayerId": Int(currentPlayerId),
            "moveCount": Int(moveCount),
            "winnerId": Int(winnerId),
            "gameStatus": gameStatus
        ]
        roomsRef.child(roomCode).updateChildValues(updates) { error, _ in
            if let error = error {
                onError(error.localizedDescription)
            } else {
                onSuccess()
            }
        }
    }

    // MARK: - Listen to Room

    func listenToRoom(
        roomCode: String,
        onUpdate: @escaping ([String: Any]) -> Void,
        onError: @escaping (String) -> Void
    ) {
        stopListening(roomCode: roomCode)

        let handle = roomsRef.child(roomCode).observe(.value) { snapshot in
            guard snapshot.exists(), let value = snapshot.value as? [String: Any] else {
                return
            }

            var data: [String: Any] = [:]
            data["hostUid"] = value["hostUid"] as? String ?? ""
            data["guestUid"] = value["guestUid"] as? String ?? ""
            data["status"] = value["status"] as? String ?? "WAITING"
            data["gridSize"] = value["gridSize"] as? Int ?? 6
            data["gameVariant"] = value["gameVariant"] as? String ?? "CLASSIC"
            data["hostName"] = value["hostName"] as? String ?? "Player 1"
            data["guestName"] = value["guestName"] as? String ?? "Player 2"
            data["hostColorIndex"] = value["hostColorIndex"] as? Int ?? 0
            data["guestColorIndex"] = value["guestColorIndex"] as? Int ?? 1
            data["currentPlayerId"] = value["currentPlayerId"] as? Int ?? 1
            data["moveCount"] = value["moveCount"] as? Int ?? 0
            data["winnerId"] = value["winnerId"] as? Int ?? 0
            data["gameStatus"] = value["gameStatus"] as? String ?? "IN_PROGRESS"
            data["lastMoveRow"] = value["lastMoveRow"] as? Int ?? -1
            data["lastMoveCol"] = value["lastMoveCol"] as? Int ?? -1
            data["lastMoveBy"] = value["lastMoveBy"] as? Int ?? 0
            data["lastMoveTimestamp"] = value["lastMoveTimestamp"] as? Int64 ?? 0

            // Board is an array of strings
            if let boardArray = value["board"] as? [String] {
                data["board"] = boardArray
            } else {
                data["board"] = [String]()
            }

            onUpdate(data)
        }

        listeners[roomCode] = handle
    }

    func stopListening(roomCode: String) {
        if let handle = listeners[roomCode] {
            roomsRef.child(roomCode).removeObserver(withHandle: handle)
            listeners.removeValue(forKey: roomCode)
        }
    }

    // MARK: - Leave Room

    func leaveRoom(
        roomCode: String,
        onSuccess: @escaping () -> Void, onError: @escaping (String) -> Void
    ) {
        let uid = getUid()
        roomsRef.child(roomCode).getData { [weak self] error, snapshot in
            guard let self = self, let snapshot = snapshot, snapshot.exists() else {
                onSuccess()
                return
            }

            let hostUid = snapshot.childSnapshot(forPath: "hostUid").value as? String ?? ""
            let status = snapshot.childSnapshot(forPath: "status").value as? String ?? ""

            if status == "WAITING" && hostUid == uid {
                self.roomsRef.child(roomCode).removeValue { error, _ in
                    if let error = error { onError(error.localizedDescription) }
                    else { onSuccess() }
                }
            } else {
                self.roomsRef.child(roomCode).child("status").setValue("FINISHED") { error, _ in
                    if let error = error { onError(error.localizedDescription) }
                    else { onSuccess() }
                }
            }
        }
    }
}

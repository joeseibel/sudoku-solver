let board = "004007830000050470720030695080700300649513728007008010470080060016040007005276100"
let optionalBoard = parse(optionalBoard: board)
try print(bruteForce(board: optionalBoard))
let board = "004007830000050470720030695080700300649513728007008010470080060016040007005276100"
let optionalBoard = Board(optionalBoard: board)
try print(bruteForce(board: optionalBoard))
let cellBoard = Board(toCellBoard: optionalBoard)
print(cellBoard)
print(Board(simpleBoard: "000105000140000670080002400063070010900000003010090520007200080026000035000409000"))

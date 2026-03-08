import Testing

struct BruteForceTest {
    @Test func testBruteForceSingleSolution() throws {
        let board = "010040560230615080000800100050020008600781005900060020006008000080473056045090010"
        let expected = "817942563234615789569837142451329678623781495978564321796158234182473956345296817"
        #expect(try Board(board: expected) == bruteForce(board: Board(optionalBoard: board)))
    }

    @Test func testBruteForceNoSolutions() {
        let board = "710040560230615080000800100050020008600781005900060020006008000080473056045090010"
        #expect(throws: BruteForceError.noSolutions) { try bruteForce(board: Board(optionalBoard: board)) }
    }

    @Test func testBruteForceMultipleSolutions() {
        let board = "000000560230615080000800100050020008600781005900060020006008000080473056045090010"
        #expect(throws: BruteForceError.multipleSolutions) { try bruteForce(board: Board(optionalBoard: board)) }
    }

    @Test func testBruteForceAlreadySolved() throws {
        let board = "817942563234615789569837142451329678623781495978564321796158234182473956345296817"
        #expect(try Board(board: board) == bruteForce(board: Board(optionalBoard: board)))
    }

    @Test func testBruteForceInvalidSolution() {
        let board = "817942563234615789569837142451329678623781495978564321796158234182473956345296818"
        #expect(throws: BruteForceError.noSolutions) { try bruteForce(board: Board(optionalBoard: board)) }
    }
}

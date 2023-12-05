import XCTest

final class BruteForceTest: XCTestCase {
    func testBruteForceSingleSolution() {
        let board = "010040560230615080000800100050020008600781005900060020006008000080473056045090010"
        let expected = "817942563234615789569837142451329678623781495978564321796158234182473956345296817"
        XCTAssertNoThrow(XCTAssertEqual(Board(board: expected), try bruteForce(board: Board(optionalBoard: board))))
    }
    
    func testBruteForceNoSolutions() {
        let board = "710040560230615080000800100050020008600781005900060020006008000080473056045090010"
        XCTAssertThrowsError(try bruteForce(board: Board(optionalBoard: board))) {
            XCTAssertEqual(.noSolutions, $0 as! BruteForceError)
        }
    }
    
    func testBruteForceMultipleSolutions() {
        let board = "000000560230615080000800100050020008600781005900060020006008000080473056045090010"
        XCTAssertThrowsError(try bruteForce(board: Board(optionalBoard: board))) {
            XCTAssertEqual(.multipleSolutions, $0 as! BruteForceError)
        }
    }
    
    func testBruteForceAlreadySolved() {
        let board = "817942563234615789569837142451329678623781495978564321796158234182473956345296817"
        XCTAssertNoThrow(XCTAssertEqual(Board(board: board), try bruteForce(board: Board(optionalBoard: board))))
    }
    
    func testBruteForceInvalidSolution() {
        let board = "817942563234615789569837142451329678623781495978564321796158234182473956345296818"
        XCTAssertThrowsError(try bruteForce(board: Board(optionalBoard: board))) {
            XCTAssertEqual(.noSolutions, $0 as! BruteForceError)
        }
    }
}

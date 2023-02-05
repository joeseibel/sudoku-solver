package sudokusolver.javanostreams;

import sudokusolver.javanostreams.logic.BruteForce;
import sudokusolver.javanostreams.logic.MultipleSolutionsException;
import sudokusolver.javanostreams.logic.NoSolutionsException;
import sudokusolver.javanostreams.logic.diabolical.AlignedPairExclusion;
import sudokusolver.javanostreams.logic.diabolical.BUG;
import sudokusolver.javanostreams.logic.diabolical.ExtendedUniqueRectangles;
import sudokusolver.javanostreams.logic.diabolical.HiddenUniqueRectangles;
import sudokusolver.javanostreams.logic.diabolical.Jellyfish;
import sudokusolver.javanostreams.logic.diabolical.Medusa;
import sudokusolver.javanostreams.logic.diabolical.UniqueRectangles;
import sudokusolver.javanostreams.logic.diabolical.WXYZWing;
import sudokusolver.javanostreams.logic.diabolical.XCycles;
import sudokusolver.javanostreams.logic.diabolical.XYChains;
import sudokusolver.javanostreams.logic.extreme.AlternatingInferenceChains;
import sudokusolver.javanostreams.logic.extreme.EmptyRectangles;
import sudokusolver.javanostreams.logic.extreme.FinnedSwordfish;
import sudokusolver.javanostreams.logic.extreme.FinnedXWing;
import sudokusolver.javanostreams.logic.extreme.GroupedXCycles;
import sudokusolver.javanostreams.logic.extreme.SueDeCoq;
import sudokusolver.javanostreams.logic.simple.BoxLineReduction;
import sudokusolver.javanostreams.logic.simple.HiddenPairs;
import sudokusolver.javanostreams.logic.simple.HiddenQuads;
import sudokusolver.javanostreams.logic.simple.HiddenSingles;
import sudokusolver.javanostreams.logic.simple.HiddenTriples;
import sudokusolver.javanostreams.logic.simple.NakedPairs;
import sudokusolver.javanostreams.logic.simple.NakedQuads;
import sudokusolver.javanostreams.logic.simple.NakedSingles;
import sudokusolver.javanostreams.logic.simple.NakedTriples;
import sudokusolver.javanostreams.logic.simple.PointingPairsPointingTriples;
import sudokusolver.javanostreams.logic.simple.PruneCandidates;
import sudokusolver.javanostreams.logic.tough.SimpleColoring;
import sudokusolver.javanostreams.logic.tough.Swordfish;
import sudokusolver.javanostreams.logic.tough.XWing;
import sudokusolver.javanostreams.logic.tough.XYZWing;
import sudokusolver.javanostreams.logic.tough.YWing;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class SudokuSolver {
    private static final List<Function<Board<Cell>, List<? extends BoardModification>>> SOLUTIONS = List.of(
            //Start of simple solutions.
            PruneCandidates::pruneCandidates,
            NakedSingles::nakedSingles,
            HiddenSingles::hiddenSingles,
            NakedPairs::nakedPairs,
            NakedTriples::nakedTriples,
            HiddenPairs::hiddenPairs,
            HiddenTriples::hiddenTriples,
            NakedQuads::nakedQuads,
            HiddenQuads::hiddenQuads,
            PointingPairsPointingTriples::pointingPairsPointingTriples,
            BoxLineReduction::boxLineReduction,
            //Start of tough solutions.
            XWing::xWing,
            SimpleColoring::simpleColoringRule2,
            SimpleColoring::simpleColoringRule4,
            YWing::yWing,
            Swordfish::swordfish,
            XYZWing::xyzWing,
            //Start of diabolical solutions.
            XCycles::xCyclesRule1,
            XCycles::xCyclesRule2,
            XCycles::xCyclesRule3,
            b -> BUG.bug(b).map(List::of).orElseGet(Collections::emptyList),
            XYChains::xyChains,
            Medusa::medusaRule1,
            Medusa::medusaRule2,
            Medusa::medusaRule3,
            Medusa::medusaRule4,
            Medusa::medusaRule5,
            Medusa::medusaRule6,
            Jellyfish::jellyfish,
            UniqueRectangles::uniqueRectanglesType1,
            UniqueRectangles::uniqueRectanglesType2,
            UniqueRectangles::uniqueRectanglesType3,
            UniqueRectangles::uniqueRectanglesType3BWithTriplePseudoCells,
            UniqueRectangles::uniqueRectanglesType4,
            UniqueRectangles::uniqueRectanglesType5,
            ExtendedUniqueRectangles::extendedUniqueRectangles,
            HiddenUniqueRectangles::hiddenUniqueRectangles,
            WXYZWing::wxyzWing,
            AlignedPairExclusion::alignedPairExclusion,
            //Start of extreme solutions.
            GroupedXCycles::groupedXCyclesRule1,
            GroupedXCycles::groupedXCyclesRule2,
            GroupedXCycles::groupedXCyclesRule3,
            EmptyRectangles::emptyRectangles,
            FinnedXWing::finnedXWing,
            FinnedSwordfish::finnedSwordfish,
            AlternatingInferenceChains::alternatingInferenceChainsRule1,
            AlternatingInferenceChains::alternatingInferenceChainsRule2,
            AlternatingInferenceChains::alternatingInferenceChainsRule3,
            SueDeCoq::sueDeCoq
    );

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage: java sudokusolver.javanostreams.SudokuSolver board");
        } else {
            var board = args[0];
            var showError = board.length() != Board.UNIT_SIZE_SQUARED;
            for (int i = 0; !showError && i < board.length(); i++) {
                if (board.charAt(i) < '0' || board.charAt(i) > '9') {
                    showError = true;
                    break;
                }
            }
            if (showError) {
                System.out.println("board must be " + Board.UNIT_SIZE_SQUARED + " numbers with blanks expressed as 0");
            } else {
                try {
                    System.out.println(solve(BoardFactory.parseOptionalBoard(board)));
                } catch (NoSolutionsException e) {
                    System.out.println("No Solutions");
                } catch (MultipleSolutionsException e) {
                    System.out.println("Multiple Solutions");
                } catch (UnableToSolveException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public static Board<SudokuNumber> solve(
            Board<Optional<SudokuNumber>> input
    ) throws NoSolutionsException, MultipleSolutionsException, UnableToSolveException {
        var bruteForceSolution = BruteForce.bruteForce(input);
        var board = BoardFactory.createCellBoard(input);
        List<? extends BoardModification> modifications;
        do {
            var allSolved = true;
            for (var cell : board.getCells()) {
                if (cell instanceof UnsolvedCell) {
                    allSolved = false;
                    break;
                }
            }
            if (allSolved) {
                return bruteForceSolution;
            }
            modifications = performNextSolution(board);
            for (var modification : modifications) {
                var row = modification.row();
                var column = modification.column();
                var cell = board.get(row, column);
                if (cell instanceof UnsolvedCell unsolved) {
                    var knownSolution = bruteForceSolution.get(row, column);
                    if (modification instanceof RemoveCandidates removeCandidates) {
                        for (var candidate : removeCandidates.candidates()) {
                            if (candidate == knownSolution) {
                                var message = "Cannot remove candidate " + candidate + " from [" + row + ", " + column +
                                        ']';
                                throw new IllegalStateException(message);
                            }
                            if (!unsolved.candidates().contains(candidate)) {
                                var message = candidate + " is not a candidate of [" + row + ", " + column + ']';
                                throw new IllegalStateException(message);
                            }
                        }
                        unsolved.candidates().removeAll(removeCandidates.candidates());
                    } else if (modification instanceof SetValue setValue) {
                        var value = setValue.value();
                        if (value != knownSolution) {
                            var message = "Cannot set value " + value + " to [" + row + ", " + column +
                                    "]. Solution is " + knownSolution;
                            throw new IllegalStateException(message);
                        }
                        board.set(row, column, new SolvedCell(row, column, value));
                    }
                } else if (cell instanceof SolvedCell) {
                    throw new IllegalStateException("[" + row + ", " + column + "] is already solved.");
                }
            }
        } while (!modifications.isEmpty());
        throw new UnableToSolveException(board);
    }

    private static List<? extends BoardModification> performNextSolution(Board<Cell> board) {
        for (var solution : SOLUTIONS) {
            var modifications = solution.apply(board);
            if (!modifications.isEmpty()) {
                return modifications;
            }
        }
        return Collections.emptyList();
    }
}
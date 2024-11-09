package sudokusolver.java;

import sudokusolver.java.logic.BruteForce;
import sudokusolver.java.logic.MultipleSolutionsException;
import sudokusolver.java.logic.NoSolutionsException;
import sudokusolver.java.logic.diabolical.AlignedPairExclusion;
import sudokusolver.java.logic.diabolical.BUG;
import sudokusolver.java.logic.diabolical.ExtendedUniqueRectangles;
import sudokusolver.java.logic.diabolical.HiddenUniqueRectangles;
import sudokusolver.java.logic.diabolical.Jellyfish;
import sudokusolver.java.logic.diabolical.Medusa;
import sudokusolver.java.logic.diabolical.UniqueRectangles;
import sudokusolver.java.logic.diabolical.WXYZWing;
import sudokusolver.java.logic.diabolical.XCycles;
import sudokusolver.java.logic.diabolical.XYChains;
import sudokusolver.java.logic.extreme.AlternatingInferenceChains;
import sudokusolver.java.logic.extreme.EmptyRectangles;
import sudokusolver.java.logic.extreme.FinnedSwordfish;
import sudokusolver.java.logic.extreme.FinnedXWing;
import sudokusolver.java.logic.extreme.GroupedXCycles;
import sudokusolver.java.logic.extreme.SueDeCoq;
import sudokusolver.java.logic.simple.BoxLineReduction;
import sudokusolver.java.logic.simple.HiddenPairs;
import sudokusolver.java.logic.simple.HiddenQuads;
import sudokusolver.java.logic.simple.HiddenSingles;
import sudokusolver.java.logic.simple.HiddenTriples;
import sudokusolver.java.logic.simple.NakedPairs;
import sudokusolver.java.logic.simple.NakedQuads;
import sudokusolver.java.logic.simple.NakedSingles;
import sudokusolver.java.logic.simple.NakedTriples;
import sudokusolver.java.logic.simple.PointingPairsPointingTriples;
import sudokusolver.java.logic.simple.PruneCandidates;
import sudokusolver.java.logic.tough.SimpleColoring;
import sudokusolver.java.logic.tough.Swordfish;
import sudokusolver.java.logic.tough.XWing;
import sudokusolver.java.logic.tough.XYZWing;
import sudokusolver.java.logic.tough.YWing;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class SudokuSolver {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("usage: java sudokusolver.java.SudokuSolver board");
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
            if (board.getCells().stream().noneMatch(UnsolvedCell.class::isInstance)) {
                return bruteForceSolution;
            }
            modifications = performNextSolution(board);
            modifications.forEach(modification -> {
                var row = modification.row();
                var column = modification.column();
                var cell = board.get(row, column);
                if (cell instanceof UnsolvedCell unsolved) {
                    var knownSolution = bruteForceSolution.get(row, column);
                    if (modification instanceof RemoveCandidates removeCandidates) {
                        removeCandidates.candidates().forEach(candidate -> {
                            if (candidate == knownSolution) {
                                var message = "Cannot remove candidate " + candidate + " from [" + row + ", " + column +
                                        ']';
                                throw new IllegalStateException(message);
                            }
                            if (!unsolved.candidates().contains(candidate)) {
                                var message = candidate + " is not a candidate of [" + row + ", " + column + ']';
                                throw new IllegalStateException(message);
                            }
                        });
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
            });
        } while (!modifications.isEmpty());
        throw new UnableToSolveException(board);
    }

    private static List<? extends BoardModification> performNextSolution(Board<Cell> board) {
        Stream<Function<Board<Cell>, List<? extends BoardModification>>> solutions = Stream.of(
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
        return solutions.map(solution -> solution.apply(board))
                .filter(modifications -> !modifications.isEmpty())
                .findFirst()
                .orElse(Collections.emptyList());
    }
}
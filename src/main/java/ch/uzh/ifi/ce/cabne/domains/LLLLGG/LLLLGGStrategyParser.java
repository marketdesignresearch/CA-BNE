package ch.uzh.ifi.ce.cabne.domains.LLLLGG;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.opencsv.CSVReader;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import ch.uzh.ifi.ce.cabne.strategy.GridStrategy2D;
import ch.uzh.ifi.ce.cabne.strategy.GridStrategy2DSymmetric;


public class LLLLGGStrategyParser {
    public List<GridStrategy2D> parse(Path bneFile) throws IOException {
        List<String[]> lines;
        try (Reader reader = Files.newBufferedReader(bneFile)) {
            try (CSVReader csvReader = new CSVReader(reader)) {

                lines = csvReader.readAll();

            }
        }
        if (lines.get(0)[0].equals("Iteration")) {
            lines.remove(0);
        }
        int matrixDim = (lines.size() - 4) / 3;
        //int matrixDim = (lines.size() - 3) / 4;
        List<String[]> leftLines = lines.subList(1, 2 + matrixDim);
        List<String[]> rightLines = lines.subList(3 + matrixDim, 4 + matrixDim * 2);
        List<String[]> globalLines = lines.subList(5 + matrixDim * 2, lines.size());
        RealMatrix leftLocalMatrix = parseMatrix(leftLines);
        RealMatrix rightLocalMatrix = parseMatrix(rightLines);
        RealMatrix leftGlobalMatrix = parseMatrix(globalLines);
        GridStrategy2D localStrat = new GridStrategy2D(leftLocalMatrix, rightLocalMatrix, 1.0, 1.0);
        GridStrategy2D globalStrat = new GridStrategy2DSymmetric(leftGlobalMatrix, 2.0, 2.0);

		List<GridStrategy2D> strats = new ArrayList<>(6);
		strats.add(0, localStrat);
		strats.add(1, localStrat);
		strats.add(2, localStrat);
		strats.add(3, localStrat);
		strats.add(4, globalStrat);
		strats.add(5, globalStrat);
		return strats;
    }

    private RealMatrix parseMatrix(List<String[]> lines) {
        Function<String[], double[]> stringParser = arr -> Arrays.stream(arr).mapToDouble(Double::valueOf).toArray();
        double[][] array = lines.stream().map(stringParser).toArray(double[][]::new);
        return MatrixUtils.createRealMatrix(array);
    }

}
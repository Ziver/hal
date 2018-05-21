package zutil.ml;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases are from the Machine Learning course on coursera.
 * https://www.coursera.org/learn/machine-learning/discussions/all/threads/0SxufTSrEeWPACIACw4G5w
 */
public class LinearRegressionTest {

    @Test
    public void calculateHypotesis() {
        double[] hypotesis = LinearRegression.calculateHypothesis(
                /* x */ new double[][]{{1, 2, 3}, {1, 3, 4}, {1, 4, 5}, {1, 5, 6}},
                /* theta */ new double[]{0.1, 0.2, 0.3}
        );

        assertArrayEquals(new double[]{1.4, 1.9, 2.4, 2.9}, hypotesis, 0.001);
    }

    @Test
    public void calculateCost() {
        double cost = LinearRegression.calculateCost(
                /* x */ new double[][]{{1, 2}, {1, 3}, {1, 4}, {1, 5}},
                /* y */ new double[]{7, 6, 5, 4},
                /* theta */ new double[]{0.1, 0.2}
        );

        assertEquals(11.9450, cost, 0.0001);
    }

    @Test
    public void gradientAscent() {
        double[] theta = LinearRegression.gradientDescent( // one iteration
                /* x */ new double[][]{{1, 5},{1, 2},{1, 4},{1, 5}},
                /* y */ new double[]{1, 6, 4, 2},
                /* theta */ new double[]{0, 0},
                /* alpha */0.01);

        assertArrayEquals(new double[]{0.032500, 0.107500}, theta, 0.000001);
    }
}
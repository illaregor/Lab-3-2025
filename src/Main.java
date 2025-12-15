import functions.FunctionPoint;
import functions.TabulatedFunction;
import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.FunctionPointIndexOutOfBoundsException;
import functions.InappropriateFunctionPointException;

public class Main {

    // Проверка исключений
    public static void testExceptions(TabulatedFunction func, String name) {
        System.out.println("\n--- Тестирование исключений для " + name + " ---");

        // 1. IllegalArgumentException — конструктор
        try {
            new ArrayTabulatedFunction(10, 0, 5);
        } catch (IllegalArgumentException e) {
            System.out.println("Конструктор (OK): " + e.getMessage());
        }

        // 2. IllegalStateException — deletePoint при size <= 2
        try {
            double[] values = {1, 2};
            TabulatedFunction shortFunc = new ArrayTabulatedFunction(0, 1, values);
            shortFunc.deletePoint(0);
        } catch (IllegalStateException e) {
            System.out.println("deletePoint (OK): " + e.getMessage());
        }

        // 3. FunctionPointIndexOutOfBoundsException
        try {
            func.getPoint(100);
        } catch (FunctionPointIndexOutOfBoundsException e) {
            System.out.println("getPoint (OK): " + e.getMessage());
        }

        // 4. InappropriateFunctionPointException — addPoint
        try {
            func.addPoint(new FunctionPoint(func.getPointX(1), 50));
        } catch (InappropriateFunctionPointException e) {
            System.out.println("addPoint (OK): " + e.getMessage());
        }

        // 5. InappropriateFunctionPointException — setPointX
        try {
            func.setPointX(1, func.getPointX(0) - 1);
        } catch (InappropriateFunctionPointException e) {
            System.out.println("setPointX (OK): " + e.getMessage());
        }

        System.out.println("Тестирование исключений завершено.");
    }

    // Проверка добавления, удаления и замены  нулевой точки
    public static void testZeroIndexOperations(TabulatedFunction func, String name) {
        System.out.println("\n--- Проверка операций с нулевой точкой: " + name + " ---");

        // 1. Замена нулевой точки
        try {
            FunctionPoint oldPoint = func.getPoint(0);
            FunctionPoint newPoint =
                    new FunctionPoint(oldPoint.getX(), oldPoint.getY() + 100);

            func.setPoint(0, newPoint);
            System.out.println("setPoint(0) OK: y = " + func.getPointY(0));
        } catch (Exception e) {
            System.out.println("setPoint(0) ERROR: " + e.getMessage());
        }

        // 2. Удаление нулевой точки
        try {
            int before = func.getPointsCount();
            func.deletePoint(0);
            int after = func.getPointsCount();

            System.out.println("deletePoint(0) OK: " + before + " -> " + after);
            System.out.println("Новая первая точка: x = " + func.getPointX(0));
        } catch (Exception e) {
            System.out.println("deletePoint(0) ERROR: " + e.getMessage());
        }

        // 3. Добавление новой первой точки
        try {
            double newX = func.getLeftDomainBorder() - 1;
            FunctionPoint newPoint = new FunctionPoint(newX, 999);

            func.addPoint(newPoint);
            System.out.println("addPoint(new first) OK: x = " + func.getPointX(0));
        } catch (Exception e) {
            System.out.println("addPoint(new first) ERROR: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        double[] values = {10, 20, 30, 40, 50};

        // Проверка обычной работы
        TabulatedFunction arrayFunc =
                new ArrayTabulatedFunction(0, 4, values);
        TabulatedFunction linkedFunc =
                new LinkedListTabulatedFunction(0, 4, values);

        System.out.println("--- Проверка работы ArrayTabulatedFunction ---");
        System.out.println("f(1.5) = " + arrayFunc.getFunctionValue(1.5));

        System.out.println("\n--- Проверка работы LinkedListTabulatedFunction ---");
        System.out.println("Количество точек: " + linkedFunc.getPointsCount());
        System.out.println("Правая граница: " + linkedFunc.getRightDomainBorder());
        System.out.println("f(2.5) = " + linkedFunc.getFunctionValue(2.5));

        //Проверка исключений
        testExceptions(arrayFunc, "ArrayTabulatedFunction");
        testExceptions(linkedFunc, "LinkedListTabulatedFunction");

        //Проверка нулевой точки
        testZeroIndexOperations(
                new ArrayTabulatedFunction(0, 4, new double[]{10, 20, 30, 40}),
                "ArrayTabulatedFunction"
        );

        testZeroIndexOperations(
                new LinkedListTabulatedFunction(0, 4, new double[]{10, 20, 30, 40}),
                "LinkedListTabulatedFunction"
        );
    }
}

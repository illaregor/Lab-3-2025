package functions;

public class ArrayTabulatedFunction implements TabulatedFunction {

    private FunctionPoint[] points;
    private int size;

    // Константа для сравнения вещественных чисел
    private static final double EPSILON = 1e-9;

    public ArrayTabulatedFunction(double leftX, double rightX, int pointsCount) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница должна быть строго меньше правой.");
        }
        if (pointsCount < 2) {
            throw new IllegalArgumentException("Количество точек должно быть >= 2.");
        }

        this.size = pointsCount;
        this.points = new FunctionPoint[pointsCount];
        double step = (rightX - leftX) / (pointsCount - 1);

        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + i * step;
            this.points[i] = new FunctionPoint(x, 0);
        }
    }

    public ArrayTabulatedFunction(double leftX, double rightX, double[] values) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница должна быть строго меньше правой.");
        }
        if (values.length < 2) {
            throw new IllegalArgumentException("Массив значений должен содержать >= 2 элементов.");
        }

        this.size = values.length;
        this.points = new FunctionPoint[size];
        double step = (rightX - leftX) / (size - 1);

        for (int i = 0; i < size; i++) {
            double x = leftX + i * step;
            this.points[i] = new FunctionPoint(x, values[i]);
        }
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " выходит за границы [0, " + (size - 1) + "]");
        }
    }

    @Override
    public double getLeftDomainBorder() {
        return points[0].getX();
    }

    @Override
    public double getRightDomainBorder() {
        return points[size - 1].getX();
    }

    @Override
    public double getFunctionValue(double x) {
        if (x < getLeftDomainBorder() || x > getRightDomainBorder()) return Double.NaN;

        for (int i = 0; i < size - 1; i++) {
            if (x >= points[i].getX() && x <= points[i + 1].getX() + EPSILON) {
                // Линейная интерполяция
                double x0 = points[i].getX();
                double y0 = points[i].getY();
                double x1 = points[i + 1].getX();
                double y1 = points[i + 1].getY();

                if (Math.abs(x1 - x0) < EPSILON) return y0;

                return y0 + (y1 - y0) * (x - x0) / (x1 - x0);
            }
        }
        return Double.NaN;
    }

    @Override
    public int getPointsCount() {
        return size;
    }

    @Override
    public FunctionPoint getPoint(int index) {
        checkIndex(index);
        return new FunctionPoint(points[index]);
    }

    @Override
    public void setPoint(int index, FunctionPoint point) throws InappropriateFunctionPointException {
        checkIndex(index);

        double newX = point.getX();

        if (index > 0 && newX < points[index - 1].getX() + EPSILON) {
            throw new InappropriateFunctionPointException("Новая абсцисса должна быть строго больше предыдущей точки.");
        }
        if (index < size - 1 && newX > points[index + 1].getX() - EPSILON) {
            throw new InappropriateFunctionPointException("Новая абсцисса должна быть строго меньше следующей точки.");
        }

        points[index] = new FunctionPoint(point);
    }

    @Override
    public double getPointX(int index) {
        checkIndex(index);
        return points[index].getX();
    }

    @Override
    public void setPointX(int index, double x) throws InappropriateFunctionPointException {
        checkIndex(index);

        if (index > 0 && x < points[index - 1].getX() + EPSILON) {
            throw new InappropriateFunctionPointException("Новая абсцисса должна быть строго больше предыдущей точки.");
        }
        if (index < size - 1 && x > points[index + 1].getX() - EPSILON) {
            throw new InappropriateFunctionPointException("Новая абсцисса должна быть строго меньше следующей точки.");
        }
        points[index].setX(x);
    }

    @Override
    public double getPointY(int index) {
        checkIndex(index);
        return points[index].getY();
    }

    @Override
    public void setPointY(int index, double y) {
        checkIndex(index);
        points[index].setY(y);
    }

    @Override
    public void deletePoint(int index) {
        if (size <= 2) {
            throw new IllegalStateException("Невозможно удалить точку: количество точек должно быть не менее трех.");
        }

        checkIndex(index);

        for (int i = index; i < size - 1; i++) {
            points[i] = points[i + 1];
        }
        points[size - 1] = null;
        size--;
    }

    @Override
    public void addPoint(FunctionPoint point) throws InappropriateFunctionPointException {
        int pos = 0;

        while (pos < size) {
            // Проверка 3: InappropriateFunctionPointException (совпадение абсцисс)
            if (Math.abs(points[pos].getX() - point.getX()) < EPSILON) {
                throw new InappropriateFunctionPointException("Точка с такой абсциссой уже существует: " + point.getX());
            }
            if (points[pos].getX() > point.getX()) {
                break;
            }
            pos++;
        }

        if (size == points.length) {
            FunctionPoint[] newPoints = new FunctionPoint[size + 1];
            System.arraycopy(points, 0, newPoints, 0, pos);
            newPoints[pos] = new FunctionPoint(point);
            System.arraycopy(points, pos, newPoints, pos + 1, size - pos);
            points = newPoints;
        } else {
            for (int i = size; i > pos; i--) {
                points[i] = points[i - 1];
            }
            points[pos] = new FunctionPoint(point);
        }
        size++;
    }
}
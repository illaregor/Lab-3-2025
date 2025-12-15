package functions;

public class LinkedListTabulatedFunction implements TabulatedFunction {

    // Вложенный приватный класс FunctionNode
    private static class FunctionNode {
        public FunctionPoint point;
        public FunctionNode prev;
        public FunctionNode next;

        public FunctionNode(FunctionPoint point) {
            this.point = point;
            this.prev = this.next = this;
        }
    }

    private FunctionNode head;
    private int size;

    private FunctionNode lastAccessedNode;
    private int lastAccessedIndex;

    private static final double EPSILON = 1e-9;

    private void initializeList() {
        this.head = new FunctionNode(null);
        this.size = 0;
        this.lastAccessedNode = head;
        this.lastAccessedIndex = -1;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new FunctionPointIndexOutOfBoundsException("Индекс " + index + " выходит за границы [0, " + (size - 1) + "]");
        }
    }

    public LinkedListTabulatedFunction(double leftX, double rightX, int pointsCount) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница должна быть строго меньше правой.");
        }
        if (pointsCount < 2) {
            throw new IllegalArgumentException("Количество точек должно быть >= 2.");
        }

        initializeList();

        double step = (rightX - leftX) / (pointsCount - 1);

        for (int i = 0; i < pointsCount; i++) {
            double x = leftX + i * step;
            addNodeToTail(new FunctionPoint(x, 0));
        }
    }

    public LinkedListTabulatedFunction(double leftX, double rightX, double[] values) {
        if (leftX >= rightX) {
            throw new IllegalArgumentException("Левая граница должна быть строго меньше правой.");
        }
        if (values.length < 2) {
            throw new IllegalArgumentException("Массив значений должен содержать >= 2 элементов.");
        }

        initializeList();

        double step = (rightX - leftX) / (values.length - 1);

        for (int i = 0; i < values.length; i++) {
            double x = leftX + i * step;
            addNodeToTail(new FunctionPoint(x, values[i]));
        }
    }

    private FunctionNode getNodeByIndex(int index) {
        FunctionNode current;

        if (index == lastAccessedIndex) {
            return lastAccessedNode;
        }

        if (index < size / 2) {
            current = head.next;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
        } else {
            current = head.prev;
            for (int i = size - 1; i > index; i--) {
                current = current.prev;
            }
        }

        lastAccessedNode = current;
        lastAccessedIndex = index;

        return current;
    }

    private FunctionNode addNodeToTail(FunctionPoint point) {
        FunctionNode newNode = new FunctionNode(point);

        FunctionNode tail = head.prev;

        newNode.next = head;
        newNode.prev = tail;

        tail.next = newNode;
        head.prev = newNode;

        size++;

        lastAccessedNode = head;
        lastAccessedIndex = -1;

        return newNode;
    }

    private FunctionNode addNodeByIndex(int index, FunctionPoint point) {
        if (index == size) {
            return addNodeToTail(point);
        }

        FunctionNode nextNode = getNodeByIndex(index);
        FunctionNode prevNode = nextNode.prev;

        FunctionNode newNode = new FunctionNode(point);

        newNode.next = nextNode;
        newNode.prev = prevNode;

        prevNode.next = newNode;
        nextNode.prev = newNode;

        size++;

        lastAccessedNode = head;
        lastAccessedIndex = -1;

        return newNode;
    }

    private FunctionNode deleteNodeByIndex(int index) {
        FunctionNode deletedNode = getNodeByIndex(index);

        deletedNode.prev.next = deletedNode.next;
        deletedNode.next.prev = deletedNode.prev;

        deletedNode.next = null;
        deletedNode.prev = null;

        size--;

        lastAccessedNode = head;
        lastAccessedIndex = -1;

        return deletedNode;
    }

    @Override
    public double getLeftDomainBorder() {
        if (size == 0) return Double.NaN;
        return head.next.point.getX();
    }

    @Override
    public double getRightDomainBorder() {
        if (size == 0) return Double.NaN;
        return head.prev.point.getX();
    }

    @Override
    public double getFunctionValue(double x) {
        if (x < getLeftDomainBorder() || x > getRightDomainBorder()) return Double.NaN;

        FunctionNode current = head.next;
        if (lastAccessedIndex != -1) {
            current = lastAccessedNode;
        }

        while (current.next != head) {
            FunctionNode nextNode = current.next;

            if (x >= current.point.getX() && x <= nextNode.point.getX() + EPSILON) {
                lastAccessedNode = current;
                lastAccessedIndex = -1;
                // Линейная интерполяция
                double x0 = current.point.getX();
                double y0 = current.point.getY();
                double x1 = nextNode.point.getX();
                double y1 = nextNode.point.getY();

                if (Math.abs(x1 - x0) < EPSILON) return y0;

                return y0 + (y1 - y0) * (x - x0) / (x1 - x0);
            }
            current = current.next;
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
        return new FunctionPoint(getNodeByIndex(index).point);
    }

    @Override
    public void setPoint(int index, FunctionPoint point) throws InappropriateFunctionPointException {
        checkIndex(index);

        FunctionNode targetNode = getNodeByIndex(index);
        double newX = point.getX();

        if (targetNode.prev != head && newX < targetNode.prev.point.getX() + EPSILON) {
            throw new InappropriateFunctionPointException("Новая абсцисса должна быть строго больше предыдущей точки.");
        }
        if (targetNode.next != head && newX > targetNode.next.point.getX() - EPSILON) {
            throw new InappropriateFunctionPointException("Новая абсцисса должна быть строго меньше следующей точки.");
        }

        targetNode.point.setX(newX);
        targetNode.point.setY(point.getY());
    }

    @Override
    public double getPointX(int index) {
        checkIndex(index);
        return getNodeByIndex(index).point.getX();
    }

    @Override
    public void setPointX(int index, double x) throws InappropriateFunctionPointException {
        checkIndex(index);

        FunctionNode targetNode = getNodeByIndex(index);

        if (targetNode.prev != head && x < targetNode.prev.point.getX() + EPSILON) {
            throw new InappropriateFunctionPointException("Новая абсцисса должна быть строго больше предыдущей точки.");
        }
        if (targetNode.next != head && x > targetNode.next.point.getX() - EPSILON) {
            throw new InappropriateFunctionPointException("Новая абсцисса должна быть строго меньше следующей точки.");
        }
        targetNode.point.setX(x);
    }

    @Override
    public double getPointY(int index) {
        checkIndex(index);
        return getNodeByIndex(index).point.getY();
    }

    @Override
    public void setPointY(int index, double y) {
        checkIndex(index);
        getNodeByIndex(index).point.setY(y);
    }

    @Override
    public void deletePoint(int index) {
        if (size <= 2) {
            throw new IllegalStateException("Невозможно удалить точку: количество точек должно быть не менее трех.");
        }

        checkIndex(index);
        deleteNodeByIndex(index);
    }

    @Override
    public void addPoint(FunctionPoint point) throws InappropriateFunctionPointException {

        FunctionNode current = head.next;
        int pos = 0;

        while (current != head) {
            if (Math.abs(current.point.getX() - point.getX()) < EPSILON) {
                throw new InappropriateFunctionPointException("Точка с такой абсциссой уже существует: " + point.getX());
            }
            if (current.point.getX() > point.getX()) {
                break;
            }
            current = current.next;
            pos++;
        }

        addNodeByIndex(pos, point);
    }
}
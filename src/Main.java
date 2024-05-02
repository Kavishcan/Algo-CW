import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {

    static class Point {
        int x, y, g, h;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
            this.g = Integer.MAX_VALUE;
            this.h = 0;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Point point = (Point) o;
            return x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    static class Map {
        char[][] grid;
        int width, height;
        Point start, finish;

        public Map(char[][] grid, int width, int height, Point start, Point finish) {
            this.grid = grid;
            this.width = width;
            this.height = height;
            this.start = start;
            this.finish = finish;
        }

        public boolean isValidMove(Point point) {
            return point.x >= 0 && point.x < height && point.y >= 0 && point.y < width && grid[point.x][point.y] != '0';
        }

        public int calculateH(Point point) {
            return Math.abs(point.x - finish.x) + Math.abs(point.y - finish.y);
        }

        public Point slide(Point current, int dx, int dy) {
            int x = current.x, y = current.y;
            while (true) {
                int nextX = x + dx, nextY = y + dy;
                if (nextX < 0 || nextX >= height || nextY < 0 || nextY >= width || grid[nextX][nextY] == '0')
                    break;
                if (grid[nextX][nextY] == 'F' || grid[nextX][nextY] == 'S') {
                    return new Point(nextX, nextY);
                }
                x = nextX;
                y = nextY;
            }
            return new Point(x, y);
        }
    }

    public static Map parseMap(String fileName) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        List<String> lines = new ArrayList<>();
        String line;
        int width = 0;
        int height = 0;
        Point start = null;
        Point finish = null;

        while ((line = reader.readLine()) != null) {
            lines.add(line);
            width = Math.max(width, line.length());
            height++;
        }
        reader.close();

        char[][] grid = new char[height][width];
        for (int i = 0; i < height; i++) {
            line = lines.get(i);
            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);
                grid[i][j] = c;
                if (c == 'S')
                    start = new Point(i, j);
                else if (c == 'F')
                    finish = new Point(i, j);
            }
        }
        return new Map(grid, width, height, start, finish);
    }

    public static List<Point> findShortestPath(Map map) {
        java.util.Map<Point, Integer> gScore = new HashMap<>();
        java.util.Map<Point, Integer> fScore = new HashMap<>();
        java.util.Map<Point, Point> cameFrom = new HashMap<>();
        Set<Point> closedSet = new HashSet<>();

        Comparator<Point> comparator = Comparator.comparingInt(p -> fScore.getOrDefault(p, Integer.MAX_VALUE));
        PriorityQueue<Point> openSet = new PriorityQueue<>(comparator);

        Point start = map.start;
        gScore.put(start, 0);
        fScore.put(start, map.calculateH(start));
        openSet.add(start);

        while (!openSet.isEmpty()) {
            Point current = openSet.poll();
            if (current.equals(map.finish)) {
                return reconstructPath(cameFrom, start, map.finish);
            }

            closedSet.add(current);

            int[][] directions = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
            for (int[] dir : directions) {
                Point next = map.slide(current, dir[0], dir[1]);
                if (closedSet.contains(next) || !map.isValidMove(next))
                    continue;

                int tentativeGScore = gScore.getOrDefault(current, Integer.MAX_VALUE) + 1;
                if (tentativeGScore < gScore.getOrDefault(next, Integer.MAX_VALUE)) {
                    cameFrom.put(next, current);
                    gScore.put(next, tentativeGScore);
                    fScore.put(next, tentativeGScore + map.calculateH(next));
                    if (!openSet.contains(next)) {
                        openSet.add(next);
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    private static List<Point> reconstructPath(java.util.Map<Point, Point> cameFrom, Point start, Point finish) {
        LinkedList<Point> path = new LinkedList<>();
        Point current = finish;
        while (current != null) {
            path.addFirst(current);
            if (current.equals(start)) {
                break;
            }
            current = cameFrom.get(current);
        }
        return path;
    }

    public static void printPath(List<Point> path) {
        if (path.isEmpty()) {
            System.out.println("No path found.");
        } else {
            System.out.println("Path found:");
            for (int i = 0; i < path.size(); i++) {
                Point p = path.get(i);
                String direction = "";
                if (i > 0) {
                    Point prev = path.get(i - 1);
                    if (prev.x < p.x) {
                        direction = "Down";
                    } else if (prev.x > p.x) {
                        direction = "Up";
                    } else if (prev.y < p.y) {
                        direction = "Right";
                    } else if (prev.y > p.y) {
                        direction = "Left";
                    }
                }
                if (i == 0) {
                    System.out.println("1. Start at (" + (p.y + 1) + ", " + (p.x + 1) + ")");
                } else {
                    System.out.println((i + 1) + ". Move to " + direction + " (" + (p.y + 1) + ", " + (p.x + 1) + ")");
                }
            }
            System.out.println((path.size() + 1) + ". Done!");
        }
    }

    public static void main(String[] args) {
        try {
            Map map = parseMap("puzzle_1.txt");
            long startTime = System.nanoTime();
            List<Point> path = findShortestPath(map);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // convert to milliseconds
            printPath(path);
            System.out.println("Time taken: " + duration + " ms");
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

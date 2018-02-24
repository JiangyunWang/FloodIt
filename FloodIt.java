import java.util.ArrayList;
import java.util.Arrays;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import tester.Tester;

import java.util.Random;

//Design:
//The user can decide how many cells are there and how many color the cells
//might have, but the range of number of color is only from 1 to 8. If the user
//choose more than 8 colors, it will automatically take 8 colors

//--------------------------------------------------------------------------------
// Represents a single square of the game area
class Cell {
  // In logical coordinates, with the origin at the top-left corner of the
  // screen
  int x;// row
  int y;// column
  String color;
  boolean flooded;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  // initial constructor
  Cell(int x, int y, String color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
    this.left = null;
    this.top = null;
    this.right = null;
    this.bottom = null;
  }
}

// a class representing a tracking timer
class Timer {
  int min;
  int sec;

  // initial constructor
  Timer() {
    this.min = 0;
    this.sec = 0;
  }

  // constructor for testing
  Timer(int min, int sec) {
    this.min = min;
    this.sec = sec;
  }
}

// a class represents a world named FloodIt, it has:
// - board: represents the cells
// - size: represents the how many rows and columns in the world
// - kinds: represents the number of colors that cells might have
// - colors: contains the colors letting user pick
// - clicked: stores the color gotten by onMouseClicked method
// - steps: keeps track of steps user uses to make all cells flooded
// - timer: keeps track of time users uses to complete the game

class FloodItWorld extends World {
  // All the cells of the game
  ArrayList<Cell> board;
  int kinds;
  int size;
  String clicked;
  int steps;
  Timer timer;

  // constructor
  FloodItWorld(int kinds, int size) {
    this.kinds = kinds;
    this.size = size;
    this.board = this.grid();
    this.clicked = this.board.get(0).color;
    this.neighbor();
    this.steps = 0;
    this.timer = new Timer();
  }

  // EFFECT: if user presses "r", it will reset
  public void onKeyEvent(String ke) {
    if (ke.equals("r")) {
      board = this.grid();
      this.clicked = this.board.get(0).color;
      this.neighbor();
      this.steps = 0;
      this.timer = new Timer();
    }
  }

  // draws the world by adding the background, cells, score, timer, and final
  // result
  public WorldScene makeScene() {
    int limit = kinds * (size / 4 + 2); // the max steps
    WorldScene bg = new WorldScene(this.size, this.size);

    // canvas
    WorldImage blackbg = new RectangleImage((size + 1) * 20, (1 + size) * 20, "solid",
        Color.darkGray);
    bg.placeImageXY(blackbg, (size + 1) * 10, (1 + size) * 10);

    // image of cells
    for (Cell c : board) {
      bg.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, getColor(c)), c.x * 20 + 20,
          c.y * 20 + 20);
    }

    // score image
    bg.placeImageXY(
        new TextImage("Score:" + " " + Integer.toString(steps) + "/" + limit, 20, Color.BLACK),
        size * 10, (size + 2) * 20);

    // timer image
    bg.placeImageXY(new TextImage(
        "Timer:" + " " + Integer.toString(timer.min) + ":" + Integer.toString(timer.sec), 20,
        Color.BLACK), size * 10, (size + 4) * 20);

    // adds the final result by comparing the steps and max steps
    if (steps >= limit) {
      bg.placeImageXY(new TextImage("You Lose", 20, Color.black), size * 10, (size + 3) * 20);
    }
    else {
      if (allSame()) {
        bg.placeImageXY(new TextImage("You Win!", 20, Color.black), size * 10, (size + 3) * 20);
      }
    }
    return bg;
  }

  // checks if all the cells are in the same color
  public boolean allSame() {
    boolean answer = true;
    for (int i = 0; i < board.size(); i++) {
      if (!board.get(i).color.equals(board.get(0).color)) {
        answer = false;
      }
    }
    return answer;
  }

  // EFFECT: gets the color of the position where user clicks and
  // keeps counting the steps
  public void onMouseClicked(Posn pos) {
    for (Cell c : board) {
      if (c.x * 20 + 10 <= pos.x && (c.x + 1) * 20 + 10 >= pos.x && c.y * 20 + 10 <= pos.y
          && (c.y + 1) * 20 + 10 >= pos.y) {
        clicked = c.color;

      }
    }
    if (!board.get(0).color.equals(clicked)) {
      this.steps++;
    }
  }

  // EFFECT: changes the color of each cell if it is flooded
  // and keeps track the time user uses
  public void onTick() {
    this.board.get(0).flooded = true;
    for (Cell c : board) {
      if (c.flooded) {
        c.color = this.clicked;
        this.changeFlooded(c);
      }
    }

    if (!allSame()) {
      if (timer.sec == 59) {
        this.timer.min++;
        this.timer.sec = 0;
      }
      else {
        this.timer.sec++;
      }
    }
  }

  // EFFECT: change the status of cells according to the color
  public void changeFlooded(Cell c) {
    if (c.right.color.equals(this.clicked)) {
      c.right.flooded = true;
    }
    if (c.bottom.color.equals(this.clicked)) {
      c.bottom.flooded = true;
    }

    if (c.left.color.equals(this.clicked)) {
      c.left.flooded = true;
    }
    if (c.top.color.equals(this.clicked)) {
      c.top.flooded = true;
    }
  }

  // picks up the random color and make up a list of cells
  public ArrayList<Cell> grid() {
    ArrayList<String> colorLib = this.pickUpColor();
    ArrayList<Cell> temp = new ArrayList<Cell>();
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        temp.add(new Cell(i, j, colorLib.get(this.randomInt()), false));
      }
    }
    return temp;
  }

  // EFFECT: finds cells' neighbors
  public void neighbor() {
    for (Cell c : board) {
      if (c.x != size - 1) {
        c.bottom = board.get(size + board.indexOf(c));
      }
      else {
        c.bottom = c;
      }
      if (c.x != 0) {
        c.top = board.get(board.indexOf(c) - size);
      }
      else {
        c.top = c;
      }
      if (c.y != 0) {
        c.left = board.get(board.indexOf(c) - 1);
      }
      else {
        c.left = c;
      }
      if (c.y != size - 1) {
        c.right = board.get(board.indexOf(c) + 1);
      }
      else {
        c.right = c;
      }
    }
  }

  // picks up the number of color for the list of color
  // according to the *users input*
  public ArrayList<String> pickUpColor() {
    ArrayList<String> store = new ArrayList<String>(
        Arrays.asList("red", "pink", "orange", "yellow", "green", "blue", "cyan", "magenta"));
    ArrayList<String> temp = new ArrayList<String>();

    if (this.kinds > 8) {
      return store;
    }
    else {
      for (int i = 0; i < kinds; i++) {
        temp.add(store.get(i));
      }
      return temp;
    }
  }

  // transfers the color from String
  public Color getColor(Cell c) {
    if (c.color.equals("red")) {
      return Color.red;
    }
    if (c.color.equals("orange")) {
      return Color.orange;
    }
    if (c.color.equals("yellow")) {
      return Color.yellow;
    }
    if (c.color.equals("green")) {
      return Color.green;
    }
    if (c.color.equals("blue")) {
      return Color.blue;
    }
    if (c.color.equals("cyan")) {
      return Color.cyan;
    }
    if (c.color.equals("magenta")) {
      return Color.magenta;
    }
    else {
      if (c.color.equals("pink")) {
        return Color.pink;
      }
      else {
        return null;
      }
    }
  }

  // produces a random integer
  public int randomInt() {
    if (this.kinds > 7) {
      return 0 + new Random().nextInt(8);
    }
    else {
      return 0 + new Random().nextInt(this.kinds);
    }
  }

}

// a class of examples of tests
class ExampleFlood {
  // examples for cells
  Cell c1;
  Cell c2;
  Cell c3;
  Cell c4;
  Cell c5;
  Cell c6;
  Cell c7;
  Cell c8;
  Cell c9;

  // examples for ArrayList
  ArrayList<Cell> a1;
  ArrayList<Cell> a2;

  // examples for FloodItWorld
  FloodItWorld f1;
  FloodItWorld f4;
  FloodItWorld f7;
  FloodItWorld f10;

  void initialCondition() {
    // examples for cells
    c1 = new Cell(0, 0, "red", false);
    c2 = new Cell(0, 1, "pink", false);
    c3 = new Cell(0, 2, "green", false);
    c4 = new Cell(1, 0, "red", false);
    c5 = new Cell(1, 1, "orange", false);
    c6 = new Cell(1, 2, "pink", false);
    c7 = new Cell(2, 0, "orange", false);
    c8 = new Cell(2, 1, "orange", false);
    c9 = new Cell(2, 2, "blue", false);

    // examples for ArrayList<Cell>
    a1 = new ArrayList<Cell>(Arrays.asList(c1, c2, c3, c4, c5, c6, c7, c8, c9));

    // examples for FloodItWorld
    f4 = new FloodItWorld(4, 3);
    f7 = new FloodItWorld(7, 30);
    f10 = new FloodItWorld(10, 30);

    f4.board = a1;
    f4.clicked = "red";
  }

  // testing pickUpColor method
  void testpickUpColor(Tester t) {
    this.initialCondition();
    t.checkExpect(new FloodItWorld(8, 30).pickUpColor(), new ArrayList<String>(
        Arrays.asList("red", "pink", "orange", "yellow", "green", "blue", "cyan", "magenta")));
    t.checkExpect(new FloodItWorld(1, 30).pickUpColor(),
        new ArrayList<String>(Arrays.asList("red")));
    t.checkExpect(new FloodItWorld(10, 30).pickUpColor(), new ArrayList<String>(
        Arrays.asList("red", "pink", "orange", "yellow", "green", "blue", "cyan", "magenta")));
  }

  // testing randomInt method
  void testRandomInt(Tester t) {
    this.initialCondition();
    t.checkOneOf("test RandomInt()", f4.randomInt(), 1, 2, 3, 0);
    t.checkNoneOf("test RandomInt()", f4.randomInt(), 4, 5, 6, 7);
    t.checkOneOf("test RandomInt()", f10.randomInt(), 1, 2, 3, 0, 4, 5, 6, 7, 8);
    t.checkNoneOf("test RandomInt()", f4.randomInt(), 40, -5, 76, 79);
  }

  // testing getColor method
  void testgetColor(Tester t) {
    this.initialCondition();
    t.checkExpect(f4.getColor(c1), Color.red);
    t.checkExpect(f4.getColor(c8), Color.orange);
    t.checkExpect(f10.getColor(c9), Color.blue);
  }

  // testing Neighbor method
  void testNeighbor(Tester t) {
    this.initialCondition();
    for (int i = 0; i < f4.board.size(); i++) {
      t.checkExpect(f4.board.get(i).bottom, null);
      t.checkExpect(f4.board.get(i).top, null);
      t.checkExpect(f4.board.get(i).left, null);
      t.checkExpect(f4.board.get(i).right, null);
    }
    f4.neighbor();
    for (Cell c : f4.board) {
      if (c.x == f4.size - 1) {
        t.checkExpect(c.bottom, c);
      }
      if (c.x != f4.size - 1) {
        t.checkExpect(c.bottom, f4.board.get(f4.size + f4.board.indexOf(c)));
      }
      if (c.x != 0) {
        t.checkExpect(c.top, f4.board.get(f4.board.indexOf(c) - f4.size));
      }
      if (c.x == 0) {
        t.checkExpect(c.top, c.top);
      }
      if (c.y != 0) {
        t.checkExpect(c.left, f4.board.get(f4.board.indexOf(c) - 1));
      }
      if (c.y == 0) {
        t.checkExpect(c.left, c);
      }
      if (c.y != f4.size - 1) {
        t.checkExpect(c.right, f4.board.get(f4.board.indexOf(c) + 1));
      }
      if (c.y == f4.size - 1) {
        t.checkExpect(c.right, c);
      }
    }
  }

  // testing grid method
  void testGrid(Tester t) {
    this.initialCondition();
    t.checkOneOf("test Grid", f4.grid().get(0).color, "red", "pink", "orange", "yellow");
    t.checkNoneOf("test Grid", f4.grid().get(0).color, "green", "blue", "cyan", "magenta");
    t.checkOneOf("test Grid", f10.grid().get(0).color, "red", "pink", "orange", "yellow", "green",
        "blue", "cyan", "magenta");
    t.checkNoneOf("test Grid", f4.grid().get(0).color, "purple", "viloent", "brown");
  }

  // testing changeFlooded method
  void testChangeFlooded(Tester t) {
    this.initialCondition();
    f4.board = a1;
    f4.neighbor();
    f4.changeFlooded(c1);
    t.checkExpect(f4.board.get(1).flooded, false);
    t.checkExpect(c4.flooded, true);
    f4.clicked = "orange";
    f4.changeFlooded(c7);
    t.checkExpect(f4.board.get(7).flooded, true);
  }

  // testing onKey method
  void testOnKey(Tester t) {
    this.initialCondition();
    f4.onKeyEvent("r");
    t.checkExpect(f4.board.size(), 9);
    f4.board = a1;
    f4.onKeyEvent("k");
    t.checkExpect(f4.board.get(0), c1);
  }

  // testing onTick method
  void testOnTick(Tester t) {
    this.initialCondition();
    f4.board = a1;
    f4.neighbor();
    t.checkExpect(f4.board.get(0).flooded, false);
    c2.flooded = true;
    f4.onTick();
    t.checkExpect(c1.flooded, true);
    t.checkExpect(c2.color, "red");

    // checks timer
    t.checkExpect(f4.timer, new Timer(0, 1));
    f4.onTick();
    t.checkExpect(f4.timer, new Timer(0, 2));
    f10.timer = new Timer(0, 59);
    f10.onTick();
    t.checkExpect(f10.timer, new Timer(1, 0));
  }

  // testing allSame method
  void testAllSame(Tester t) {
    this.initialCondition();
    f4.neighbor();
    t.checkExpect(f4.allSame(), false);
    f4.board = new ArrayList<Cell>(Arrays.asList(c1, c4));
    t.checkExpect(f4.allSame(), true);
  }

  // testing onMouseClicked method
  void testOnMouseClicked(Tester t) {
    this.initialCondition();
    f4.neighbor();
    f4.onMouseClicked(new Posn(20, 15));
    t.checkExpect(f4.steps, 0);
    t.checkExpect(f4.clicked, "red");
    f4.onMouseClicked(new Posn(50, 10));
    t.checkExpect(f4.clicked, "orange");

    // tests steps
    t.checkExpect(f4.steps, 1);
  }

  // testing game
  void testGame(Tester t) {
    this.initialCondition();
    // The user can input the number of color and the size of the grid below
    f1 = new FloodItWorld(3, 12);
    // If you want to play the game, please uncomment out the following line!!
    // f1.bigBang((f1.size + 1) * 20, (f1.size + 5) * 20, 1);
  }

}

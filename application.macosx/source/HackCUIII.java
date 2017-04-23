import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.net.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class HackCUIII extends PApplet {



ObjectManager objMan;
boolean started;
boolean[] keyPress, keyHeld;
int[] keyLimit;
float startTime, endTime;
int gameMode;
float wid, hei;
ArrayList<String> log;
ArrayList<Character> typed;
ArrayList<Float> onLogForAmount;
ArrayList<float[]> logCol;
float logTime;
Server serv;
Client clien;
String info;
public void setup(){
  
  gameMode = -1;
  wid = 640;
  hei = 480;
  logTime = 5;
  log = new ArrayList<String>();
  onLogForAmount = new ArrayList<Float>();
  logCol = new ArrayList<float[]>();
  objMan = new ObjectManager();
  keyPress = new boolean[10];
  keyHeld = new boolean[10];
  textAlign(LEFT,TOP);
  typed = new ArrayList<Character>();
}

public void draw(){
  background(200);
  if (gameMode == 0 || gameMode == 1){
    if (!clien.active()){
      if (started) exit();
      clien = new Client(this, "127.0.0.1", 7575+((gameMode+1)%2));
    }
    else{
      started = true;
    }
    if (clien.available() > 0){
      info = clien.readString();
      objMan.lookThroughServer(info);
    }
  endTime = System.nanoTime()/1000000000.0f;
  if (startTime == 0) startTime = endTime;
  float deltaTime = endTime-startTime;
  boolean[] kPress = new boolean[10];
  boolean[] kHeld = new boolean[10];
  for (int i = 0; i < 10; i++){
    kPress[i] = keyPress[i];
    kHeld[i] = keyHeld[i];
  }
  if (started) 
    objMan.update(kPress, kHeld, deltaTime);
  objMan.draw();
  logDraw(deltaTime);
  keyReset(kPress);
  startTime = endTime;
  }
  else if (gameMode == -1){
    background(0);
    drawIntroRoom();
    if (keyPress[0] || keyPress[1]){
      if (!keyPress[1]) gameMode = 0;
      else if (!keyPress[0]) gameMode = 1;
      serv = new Server(this, 7575+gameMode);
      clien = new Client(this, "127.0.0.1", 7575+((gameMode+1)%2));
    }
  }
  while (typed.size() > 0){
    typed.remove(0);
  }
}

public void keyPressed(){
  if (!keyHeld[0] && ((key == CODED && keyCode == LEFT) || (key == 'A' || key == 'a'))){
      keyPress[0] = true;
      keyHeld[0] = true;
    }
    else if (!keyHeld[1] && ((key == CODED && keyCode == RIGHT) || (key == 'D' || key == 'd'))){
      keyPress[1] = true;
      keyHeld[1] = true;
    }
    else if (!keyHeld[2] && ((key == CODED && keyCode == UP) || (key == 'W' || key == 'w'))){
      keyPress[2] = true;
      keyHeld[2] = true;
    }
    else if (!keyHeld[3] && ((key == CODED && keyCode == DOWN) || (key == 'S' || key == 's'))){
      keyPress[3] = true;
      keyHeld[3] = true;
    }
    else if (!keyHeld[4] && ((key == CODED && (keyCode == ENTER || keyCode == RETURN)) || (key == 'E' || key == 'e'))){
      keyPress[4] = true;
      keyHeld[4] = true;
    }
    else if (!keyHeld[5] && (key == 'q' || key == 'Q' || key == ' ')){
      keyPress[5] = true;
      keyHeld[5] = true;
    }
    if (key != CODED){
      typed.add(key);
    }
}

public void keyReleased(){
  if ((key == CODED && keyCode == LEFT) || (key == 'A' || key == 'a')){
      keyHeld[0] = false;
    }
    else if ((key == CODED && keyCode == RIGHT) || (key == 'D' || key == 'd')){
      keyHeld[1] = false;
    }
    else if ((key == CODED && keyCode == UP) || (key == 'W' || key == 'w')){
      keyHeld[2] = false;
    }
    else if ((key == CODED && keyCode == DOWN) || (key == 'S' || key == 's')){
      keyHeld[3] = false;
    }
    else if ((key == CODED && (keyCode == ENTER)) || (key == 'E' || key == 'e')){
      keyHeld[4] = false;
    }
    else if (key == 'q' || key == 'Q' || key == ' '){
      keyHeld[5] = false;
    }
}

public void keyReset(boolean[] prevKeyPressed){
  for (int i = 0; i < 10; i++){
    if (prevKeyPressed[i]){
      keyPress[i] = false;
    }
  }
}

public void addToLog(String s, float[] col){
  addToLog(s, col, true);
}

public void addToLog(String s, float[] col, boolean passToOther){
  if (col.length < 3){
    col = new float[]{col[0],col[0],col[0]};
  }
  String tempStr = "";
  for (int i = 0; i < s.length(); i++){
    if (s.charAt(i) == '\n'){
      log.add(tempStr);
      onLogForAmount.add(logTime);
      logCol.add(col);
      if (passToOther){
        serv.write("AddLog: "+hex((byte)(col[0])).toString()+hex((byte)(col[1]))+hex((byte)(col[2]))+" "+tempStr+"\n");
      }
      tempStr = "";
    }
    else tempStr += s.charAt(i);
  }
  if (tempStr.length() > 0){
    log.add(tempStr);
    onLogForAmount.add(logTime);
    logCol.add(col);
    if (passToOther){
      serv.write("AddLog: "+hex((byte)(col[0])).toString()+hex((byte)(col[1]))+hex((byte)(col[2]))+" "+tempStr+"\n");
    }
  }
}

public void logDraw(float deltaTime){
  textSize(16*hei/height);
  for (int i = 0 ; i < log.size(); i++){
    onLogForAmount.set(i, onLogForAmount.get(i)-deltaTime);
    if (onLogForAmount.get(i) <= 0){
      log.remove(i);
      onLogForAmount.remove(i);
      logCol.remove(i);
      i--;
    }
    else{
      float[] col = logCol.get(i);
      fill(col[0], col[1], col[2]);
      text(log.get(i), (wid/8)*width/wid, (hei/8+i*20)*height/hei);
    }
  }
  if (!started){
    fill(0,0,0);
    if (gameMode == 0) text("Waiting for the observer...", (wid/8)*width/wid, (hei/8-20)*height/hei);
    else if (gameMode == 1) text("Waiting for the player...", (wid/8)*width/wid, (hei/8-20)*height/hei);
  }
}

public void drawIntroRoom(){
  fill(255);
  textSize(32*height/hei);
  text("Press Left to be the Player", width/8, height/2-32*height/hei);
  text("Press Right to be the Observer", width/8, height/2+32*height/hei);
}
class Instance {
  public float x, y, w, h, dX, dY;
  public boolean isSolid, isTrigger, interactable, checkCollision;
  public boolean selectable, destroyed;
  public String name, tag, description;
  public int ID;
  protected float prevX, prevY;
  public Instance(float X, float Y){
    x = X*32;
    y = Y*32;
    w = 32;
    h = 32;
    dX = 0;
    dY = 0;
    isSolid = true;
    isTrigger = false;
    name = "";
    tag = "Misc";
    selectable = true;
    description = "The Developers Messed Up";
    checkCollision = false;
    destroyed = false;
  }
  
  public void update(boolean[] keyPress, boolean[] keyHeld, float deltaTime){
    update(deltaTime);
  }
  
  public void update(Instance player, float deltaTime){
    update(deltaTime);
  }
  
  public void finishUpdate(float deltaTime){
    x += dX*deltaTime;
    y += dY*deltaTime;
  }
  
  public float[] updateViewsFromSelf(float[] view, float[] bounds){
    view[0] = x-wid/2;
    view[1] = y-hei/2;
    if (view[0] <= 0) view[0] = 0;
    else if (view[0] >= bounds[0]-wid/2) view[0] = bounds[0]-wid/2;
    if (view[1] <= 0) view[1] = 0;
    else if (view[1] >= bounds[1]-hei/2) view[1] = bounds[1]-hei/2;
    return(view);
  }
  
  public void update(float deltaTime){
    prevX = x;
    prevY = y;
    if (objMan != null && ID == 0){
      objMan.nextID++;
      ID = objMan.nextID;
      if (name == "Door") println(ID);
    }
  }
  
  public void draw(float viewX, float viewY){
    fill(0,0,0);
    rect((x-viewX)*width/wid, y-viewY*height/hei, w*width/wid, h*height/hei);
  }
  
  public void collisionCheck(Instance other, float deltaTime){
    if (x+w+dX*deltaTime > other.x+other.dX*deltaTime && x+w <= other.x && y+h+dY*deltaTime > other.y+other.dY*deltaTime && y+dY*deltaTime <= other.y+other.h+other.dY*deltaTime){
      if (isSolid && other.isSolid){
        x = other.x-w+other.dX*deltaTime;
        dX = 0;
      }
      extraCollision(other, 0);
      other.extraCollision(this, 180);
    }
    else if (x+dX*deltaTime < other.x+other.w+other.dX*deltaTime && x >= other.x+other.w && y+h+dY*deltaTime > other.y+other.dY*deltaTime && y+dY*deltaTime <= other.y+other.h+other.dY*deltaTime){
      if (isSolid && other.isSolid){
        x = other.x+other.w+other.dX*deltaTime;
        dX = 0;
      }
      extraCollision(other, 180);
      other.extraCollision(this, 0);
    }
    if (y+h+dY*deltaTime > other.y+other.dY*deltaTime && y+h <= other.y && x+w+dX*deltaTime > other.x+other.dX*deltaTime && x+dX*deltaTime <= other.x+other.w+other.dX*deltaTime){
      if (isSolid && other.isSolid){
        y = other.y-h+other.dY*deltaTime;
        dY = 0;
      }
      extraCollision(other, 270);
      other.extraCollision(this, 90);
    }
    else if (y+dY*deltaTime < other.y+other.h+other.dY*deltaTime && y >= other.y+other.h && x+w+dX*deltaTime > other.x+other.dX*deltaTime && x+dX*deltaTime <= other.x+other.w+other.dX*deltaTime){
      if (isSolid && other.isSolid){
        y = other.y+other.h+other.dY*deltaTime;
        dY = 0;
      }
      extraCollision(other, 90);
      other.extraCollision(this, 270);
    }
  }
  
  public void extraCollision(Instance i, float direction){}
}

class Player extends Instance {
  PlayerInteractive plaIn;
  public float hp;
  int prevXDir, prevYDir;
  public Player(float X, float Y){
    super(X, Y);
    name = "Player";
    tag = "Player";
    description = "This is who you're guiding.";
    interactable = true;
    checkCollision = true;
    plaIn = new PlayerInteractive(this);
    prevYDir = 1;
    prevXDir = 0;
    hp = 1;
  }
  
  public void update(boolean[] keyPress, boolean[] keyHeld, float deltaTime){
    super.update(keyPress, keyHeld, deltaTime);
    int yDir = 0;
    int xDir = 0;
    if (keyHeld[0] && !keyHeld[1]){
      dX = -180;
      xDir = -1;
    }
    else if (keyHeld[1] && !keyHeld[0]){
      dX = 180;
      xDir = 1;
    }
    else{
      dX = 0;
    }
    if (keyHeld[2] && !keyHeld[3]){
      dY = -180;
      yDir = -1;
    }
    else if (keyHeld[3] && !keyHeld[2]){
      dY = 180;
      yDir = 1;
    }
    else{
      dY = 0;
    }
    if (xDir != 0 || yDir != 0){
      prevXDir = xDir;
      prevYDir = yDir;
    }
    if (keyPress[4]){
      if (plaIn.nameOfInteraction == "Cabinet"){
        Cabinet c = (Cabinet)(plaIn.interactingWith);
        while (c.items.size() > 0){
          ((Item)(c.items.get(0))).inInventory = true;
          objMan.inv.items.add(c.items.get(0));
          if (c.items.get(0).name == "Key") addToLog("Grabbed Key to " + ((Key)(c.items.get(0))).belongsTo + ".", new float[]{200, 100, 0});
          else addToLog("Grabbed " + c.items.get(0).name + ".", new float[]{0, 0, 0});
          c.items.remove(0);
          serv.write("Cabinet: " + str(c.ID) + " Removed 0\n");
        }
      }
    }
    plaIn.x = x+32*prevXDir;
    plaIn.y = y+32*prevYDir;
    plaIn.dX = dX;
    plaIn.dY = dY;
  }
  
  public void finishUpdate(float deltaTime){
    super.finishUpdate(deltaTime);
    if (gameMode == 0){
      serv.write("PlayerPos: "+str(x)+","+str(y)+"\n");
    }
  }
  
  public void draw(float viewX, float viewY){
    noStroke();
    fill(255,255,255);
    rect((x-viewX)*width/wid, y-viewY*height/hei, w*width/wid, h*height/hei);
  }
  
  public void extraCollision(Instance i, float direction){
    if (i.tag == "Item"){
      objMan.inv.AddTo((Item)i);
      serv.write("Inventory: Added " + i.ID + "\n");
    }
    plaIn.dX = dX;
    plaIn.dY = dY;
  }
}
class PlayerInteractive extends Instance{
  private boolean visible, interacting;
  public boolean dontPlaceStuff;
  public String nameOfInteraction;
  public Instance interactingWith;
  public PlayerInteractive(Player pla){
    super(pla.x/32, pla.y/32);
    selectable = false;
    interactable = true;
    visible = false;
    checkCollision = true;
    dontPlaceStuff = false;
  }
  
  public void update(boolean[] keyPress, boolean[] keyHeld, float deltaTime){
    super.update(keyPress, keyHeld, deltaTime);
    if (keyPress[4]) interacting = true;
    else interacting = false;
    dontPlaceStuff = false;
    nameOfInteraction = "";
    interactingWith = null;
  }
  
  public void collisionCheck(Instance other, float deltaTime){
    if (x+dX*deltaTime < other.x+other.w+other.dX*deltaTime && x+w+dX*deltaTime > other.x+other.dX*deltaTime && y+dY*deltaTime < other.y+other.h+other.dY*deltaTime && y+h+dY*deltaTime > other.y+other.dY*deltaTime){
      extraCollision(other, 0);
    }
  }
  
  public void extraCollision(Instance i, float direction){
    interactingWith = i;
    if (i.tag == "Solid"){
      dontPlaceStuff = true;
    }
    else if (i.tag == "Door" && !i.checkCollision && interacting){
      i.destroyed = true;
      addToLog("The door has been opened!", new float[]{0,0,0});
    }
    nameOfInteraction = i.name;
  }
  
  public void draw(float viewX, float viewY){
     if (visible || objMan.inv.focus){
       stroke(255,0,0);
       noFill();
       strokeWeight(2);
       rect((x-viewX)*wid/width, y-viewY*height/hei, w*width/wid, h*height/hei);
       noStroke();
     }
  }
  
  public void finishUpdate(float deltaTime){
    super.finishUpdate(deltaTime);
    if (gameMode == 0){
      serv.write("PlayerInteractivePos: "+str(x)+","+str(y)+"\n");
    }
  }
}

class Solid extends Instance{
  public Solid(float X, float Y){
    super(X, Y);
    name = "Solid";
    tag = "Solid";
    interactable = false;
    selectable = false;
  }
}

class Door extends Instance{
  public Door(float X, float Y, boolean lock){
    super(X, Y);
    checkCollision = lock;
    name = "Door";
    tag = "Door";
    UpdateDescription();
  }
  
  public void collisionCheck(Instance other, float deltaTime){
    if (other.name.equals("Key") && ((Key)other).belongsTo.equals(name) && x+dX*deltaTime < other.x+other.w+other.dX*deltaTime && x+w+dX*deltaTime > other.x+other.dX*deltaTime && y+dY*deltaTime < other.y+other.h+other.dY*deltaTime && y+h+dY*deltaTime > other.y+other.dY*deltaTime){
      checkCollision = false;
      UpdateDescription();
      other.destroyed = true;
    }
  }
  
  public void UpdateDescription(){
    if (checkCollision) description = "This door needs to be unlocked by something.";
    else description = "A closed door.";
  }
  
  public void draw(float viewX, float viewY){
    noStroke();
    if (checkCollision) fill(150,150,150);
    else fill(150, 50, 0);
    rect((x-viewX)*width/wid, y-viewY*height/hei, w*width/wid, h*height/hei);
  }
}

class Inventory{
  ArrayList<Item> items;
  float yPos;
  boolean focus;
  int itemSelected;
  public Inventory(){
    items = new ArrayList<Item>();
    yPos = hei;
  }
  
  public void update(float deltaTime){
    float dest = hei;
    if (itemSelected < 0) itemSelected = 0;
    else if (itemSelected >= items.size()) itemSelected = items.size()-1;
    if (focus){
      dest = hei-32*(ceil(items.size()/(wid/32)));
    }
    if (yPos > dest){
      yPos -= 128*deltaTime;
      if (yPos < dest) yPos = dest;
    }
    else if (yPos < dest){
      yPos += 128*deltaTime;
      if (yPos > dest) yPos = dest;
    }
    for (int i = 0; i < items.size(); i++){
      items.get(i).update(deltaTime);
      if (items.get(i).destroyed){
        items.remove(i);
        i--;
      }
    }
  }
  
  public void update(boolean[] keyPress, float deltaTime){
    if (keyPress[0] && !keyPress[1]){
      itemSelected -= 1;
      if (itemSelected < 0){
        itemSelected = items.size()-1;
      }
    } else if (keyPress[1] && !keyPress[0]){
      itemSelected += 1;
      if (itemSelected >= items.size()){
        itemSelected = 0;
      }
    }
    if (keyPress[4]){
      if (!objMan.plaIn.dontPlaceStuff && items.get(itemSelected).canRemove){
        if (objMan.plaIn.nameOfInteraction == "Cabinet"){
          ((Cabinet)(objMan.plaIn.interactingWith)).items.add(items.get(itemSelected));
          serv.write("Cabinet: " + str(objMan.plaIn.interactingWith.ID) + " Added " + items.get(itemSelected).ID + "\n");
        }
        else{
          objMan.instances.add(0, items.get(itemSelected));
          serv.write("Inventory: Dropped " + itemSelected + "\n");
        }
        items.get(itemSelected).inInventory = false;
        items.get(itemSelected).x = objMan.plaIn.x;
        items.get(itemSelected).y = objMan.plaIn.y;
        items.remove(itemSelected);
        itemSelected--;
        if (itemSelected < 0) itemSelected = 0;
        focus = false;
      }
      else if (items.get(itemSelected).canRemove){
        addToLog("You can't place that item there. There's something in the way!", new float[]{0,0,0});
      }
      else if (items.get(itemSelected).name == "Bomb"){
        addToLog("I'm afraid I can't let you do that.\n Have fun exploding! :D", new float[]{255,0,0});
      }
      else{
        addToLog("You can't get rid of this item!", new float[]{0,0,0});
      }
    }
    update(deltaTime);
  }
  
  public void AddTo(Item i){
    items.add(i);
    i.inInventory = true;
  }
  
  public void draw(){
    fill(0,50,150);
    rect(0,yPos*height/hei,width,32*(ceil(items.size()/(wid/32)))*height/hei);
    for (int i = 0; i < items.size(); i++){
      items.get(i).draw(32*(i%(wid/32)), yPos-32+32*(ceil((i+1)/(wid/32))), true);
    }
    if (items.size() > 0 && focus){
      stroke(255);
      strokeWeight(2);
      noFill();
      rect(32*(itemSelected%(wid/32)), yPos-32+32*(ceil((itemSelected+1)/(wid/32))),32,32);
      noStroke();
    }
  }
}
class Interactable extends Instance{
  public Interactable(float X, float Y){
    super(X,Y);
    tag = "Interactable";
  }
  
  public void InteractedWith(){
  }
}

/**Cabinets are Items that have stuff in them. You can't actually pick them up, but you can take what's in them.*/
class Cabinet extends Interactable{
  ArrayList<Item> items;
  int prevItemSize;
  public Cabinet(float X, float Y){
    super(X,Y);
    name = "Cabinet";
    description = "May have stuff inside of it.";
    items = new ArrayList<Item>();
    prevItemSize = items.size();
  }
  
  public void UpdateDescription(){
    if (items.size() == 0){
      description = "Has no items inside of it.";
    }
    else{
      description = "Has these items inside of it:\n";
      for (int i = 0; i < items.size(); i++){
        description += "\n  " + items.get(i).name + ": " + items.get(i).description;
      }
    }
  }
  
  public void update(float deltaTime){
    super.update(deltaTime);
    int itemSize = items.size();
    if (itemSize != prevItemSize){
      prevItemSize = itemSize;
      UpdateDescription();
    }
    for (int i = 0; i < items.size(); i++){
      if (items.get(i).ID == 0){
        objMan.nextID++;
        items.get(i).ID = objMan.nextID;
      }
    }
  }
  
  public void InteractedWith(){
    super.InteractedWith();
  }
  
  public void draw(float viewX, float viewY){
    fill(200,100,0);
    rect((x-viewX)*width/wid, y-viewY*height/hei, w*width/wid, h*height/hei);
  }
  
}


class Item extends Instance{
  boolean inInventory, canRemove;
  public Item(float X, float Y){
    super(X,Y);
    tag = "Item";
    isSolid = false;
    isTrigger = true;
    canRemove = true;
  }
  
  public void draw(float viewX, float viewY){
    fill(0,0,255);
    rect((x-viewX)*wid/width, y-viewY*hei/height, w*wid/width, h*hei/height);
  }
  
  public void draw(float X, float Y, boolean onHUD){
    if (!onHUD){
      draw(X,Y);
    }
    else{
      x = X;
      y = Y;
      draw(0, 0);
    }
  }
}

class Key extends Item{
  String belongsTo;
  public Key(float X, float Y, String keyTo){
    super(X,Y);
    name = "Key";
    description = "This key unlocks something.";
    belongsTo = keyTo;
  }
  
  public void draw(float viewX, float viewY){
    fill(255,255,0);
    rect((x-viewX)*width/wid, y-viewY*height/hei, w*width/wid, h*height/hei);
  }
}

class Bomb extends Item{
  float timer, minTimer, maxTimer;
  boolean countDown;
  public Bomb(float X, float Y, float minTim, float maxTim, boolean setOff, boolean removed){
    super(X,Y);
    name = "Bomb";
    description = "I wouldn't pick this up.";
    minTimer = minTim;
    maxTimer = maxTim;
    timer = random(minTimer, maxTimer);
    countDown = setOff;
    canRemove = removed;
  }
  
  public void update(float deltaTime){
    if (inInventory && !countDown && gameMode == 0) countDown = true;
    if (countDown){
      timer -= deltaTime;
      description = "This will blow up in " + (int)timer + " seconds!";
      if (timer <= 0){
        if (!inInventory){
        }
        else{ //>:)
          ((Player)(objMan.player)).hp -= 5;
          serv.write("PlayerHP: Down 5");
        }
        destroyed = true;
      }
    }
  }
  
  public void draw(float viewX, float viewY){
    fill(50,50,50);
    rect((x-viewX)*width/wid, y-viewY*height/hei, w*width/wid, h*height/hei);
  }
}
class ObjectManager{
  private float[] view, bounds;
  private float blackout, maxBlackout;
  private ArrayList<Instance> instances;
  private int room;
  public int nextID;
  private String[] roomLoad;
  private Instance player;
  private SupervisorViewer supView;
  private TypingStuff typStf;
  public Inventory inv;
  public PlayerInteractive plaIn;
  public ObjectManager(){
    roomLoad = new String[]{"PresentationArea"};
    room = 1;
    nextID = 1;
    view = new float[2];
    bounds = new float[2];
    bounds[0] = wid;
    bounds[1] = hei;
    inv = new Inventory();
    loadRoom(room);
    maxBlackout = 3;
    blackout = 0;
    supView = new SupervisorViewer();
    typStf = new TypingStuff();
  }
  
  public void update(boolean[] keyPress, boolean[] keyHeld, float deltaTime){
    if (((Player)player).hp > 0){
      if (keyPress[5] && inv.items.size() > 0){
        inv.focus = !inv.focus;
      }
      //Update Everyone Individually...
      for (int i = 0; i < instances.size(); i++){
        if (instances.get(i).interactable){
          if (!inv.focus && gameMode == 0) instances.get(i).update(keyPress, keyHeld, deltaTime);
          else if (gameMode == 0) instances.get(i).update(new boolean[]{false, false, false, false, false, false, false, false, false, false}, new boolean[]{false, false, false, false, false, false, false, false, false, false}, deltaTime);
          else instances.get(i).update(deltaTime);
        }
        else{
          instances.get(i).update(player, deltaTime);
        }
      }
      //Work on Collision
      for (int i = 0; i < instances.size(); i++){
        if (instances.get(i).checkCollision){
          for (int j = 0; j < instances.size(); j++){
            if (i != j && (instances.get(j).isSolid || instances.get(j).isTrigger)){
              instances.get(i).collisionCheck(instances.get(j), deltaTime);
            }
          }
        }
      }
      //Finish Updating!
      for (int i = 0; i < instances.size(); i++){
        instances.get(i).finishUpdate(deltaTime);
        if (instances.get(i).destroyed || (instances.get(i).tag == "Item" && ((Item)(instances.get(i))).inInventory)){
          if (instances.get(i).destroyed){
            serv.write("Instance: Destroyed " + (instances.get(i).ID) + "\n");
          }
          instances.remove(i);
          i--;
        }
      }
      if (gameMode == 0) view = player.updateViewsFromSelf(view, bounds);
      if (!inv.focus) inv.update(deltaTime);
      else inv.update(keyPress, deltaTime);
    }
    else{
      blackout += deltaTime;
      if (blackout > maxBlackout){
        blackout = 0;
        loadRoom(0);
        inv = new Inventory();
      }
    }
    if (gameMode == 1){
      view = supView.update(keyHeld, view, bounds, deltaTime);
      typStf.update();
    }
  }
  
  public void draw(){
    for (int i = 0; i < instances.size(); i++){
      Instance in = instances.get(i);
      if (in.x+in.w >= view[0] && in.y+in.h >= view[1] && in.x <= view[0]+wid && in.y <= view[1]+hei){
        in.draw(view[0], view[1]);
        if (gameMode == 1){
          supView.draw(view[0], view[1], in);
          typStf.draw();
        }
      }
    }
    plaIn.draw(view[0], view[1]);
    inv.draw();
    fill(0,0,0,blackout*255/maxBlackout);
    rect(0,0,width,height);
  }
  
  public void loadRoom(int room){
    nextID = 0;
    instances = new ArrayList<Instance>();
    if (room == 0){
      player = new Player(wid/64, hei/64);
      plaIn = ((Player)player).plaIn;
      Cabinet c1 = new Cabinet(wid/64, 1);
      c1.items.add(new Key(0, 0, "Door"));
      c1.items.add(new Key(0, 0, "Door"));
      Cabinet c2 = new Cabinet(wid/64+2, 1);
      c2.items.add(new Bomb(0, 0, 2, 3, false, false));
      //c2.items.add();
      instances.add(c1);
      instances.add(c2);
      instances.add(player);
      instances.add(plaIn);
      instances.add(new Solid(wid/48, hei/48));
      instances.add(new Door(2,5,true));
    }
    else if (room > 0 && room <= roomLoad.length){
      String[] loading = loadStrings("Rooms/"+roomLoad[room-1]+".txt");
      int numberOfCabinets = 0;
      bounds[0] = 32*loading[0].length();
      bounds[1] = 32*loading.length;
      for (int y = 0; y < loading.length; y++){
        for (int x = 0; x < loading[y].length(); x++){
          if (loading[y].charAt(x) == 'P'){
            player = new Player(x,y);
            plaIn = ((Player)player).plaIn;
            instances.add(player);
            instances.add(plaIn);
          }
          else if (loading[y].charAt(x) == 'S') instances.add(new Solid(x,y));
          else if (loading[y].charAt(x) == 'L') instances.add(new Door(x,y,true));
          else if (loading[y].charAt(x) == 'D') instances.add(new Door(x,y,false));
          else if (loading[y].charAt(x) == 'K') instances.add(new Key(x,y,"Door"));
          else if (loading[y].charAt(x) == 'B') instances.add(new Bomb(x, y, 5, 10, true, false));
          else if (loading[y].charAt(x) == 'C'){
            Cabinet c = new Cabinet(x,y);
            if (room == 1){
              if (numberOfCabinets == 0){
                c.items.add(new Key(0, 0, "Door"));
                c.items.add(new Key(0, 0, "Door"));
                c.items.add(new Key(0, 0, "Door"));
                c.items.add(new Key(0, 0, "Door"));
              }
              else if (numberOfCabinets == 1){
                c.items.add(new Bomb(0, 0, 2, 3, false, false));
              }
            }
            numberOfCabinets++;
            instances.add(c);
          }
        }
      }
    }
  }
  
  public void lookThroughServer(String info){
    String[] infos = splitTokens(info, "\n");
    for (int i = 0; i < infos.length; i++){
      if (infos[i].length() > 15 && infos[i].substring(0,8).equals("AddLog: ")){
        float[] col = new float[3];
        col[0] = unhex(infos[i].substring(8,10));
        col[1] = unhex(infos[i].substring(10,12));
        col[2] = unhex(infos[i].substring(12,14));
        addToLog(infos[i].substring(15), col, false);
      }
      else if (infos[i].length() > 11 && infos[i].substring(0,11).equals("PlayerPos: ")){
        String[] positionMarker = splitTokens(infos[i].substring(11), ",");
        if (positionMarker.length > 1){
          player.x = PApplet.parseFloat(positionMarker[0]);
          player.y = PApplet.parseFloat(positionMarker[1]);
        }
      }
      else if (infos[i].length() > 22 && infos[i].substring(0,22).equals("PlayerInteractivePos: ")){
        String[] positionMarker = splitTokens(infos[i].substring(22), ",");
        if (positionMarker.length > 1){
          plaIn.x = PApplet.parseFloat(positionMarker[0]);
          plaIn.y = PApplet.parseFloat(positionMarker[1]);
        }
      }
      else if (infos[i].length() > 11 && infos[i].substring(0,11).equals("Inventory: ")){
        String[] instructions = splitTokens(infos[i].substring(11), " ");
        if (instructions[0].equals("Added")){
          int id = PApplet.parseInt(instructions[1]);
          for (int j = 0; j < instances.size(); j++){
            if (instances.get(j).ID == id){
              ((Item)instances.get(j)).inInventory = true;
              inv.items.add((Item)instances.get(j));
              break;
            }
          }
        }
        else if (instructions[0].equals("Dropped")){
          int id = PApplet.parseInt(instructions[1]);
          if (inv.items.size() > id){
            inv.items.get(id).inInventory = false;
            inv.items.get(id).x = plaIn.x;
            inv.items.get(id).y = plaIn.y;
            objMan.instances.add(0, inv.items.get(id));
            inv.items.remove(id);
          }
        }
      }
      else if (infos[i].length() > 9 && infos[i].substring(0,9).equals("Cabinet: ")){
        String[] instructions = splitTokens(infos[i].substring(9), " ");
        int id = PApplet.parseInt(instructions[0]);
        for (int j = 0; j < instances.size(); j++){
          if (instances.get(j).ID == id){
            Cabinet c = (Cabinet)(instances.get(j));
            int id2 = PApplet.parseInt(instructions[2]);
            if (instructions[1].equals("Added")){
              for (int k = 0; k < inv.items.size(); k++){
                if (inv.items.get(k).ID == id2){
                  c.items.add(inv.items.get(k));
                  inv.items.get(k).inInventory = false;
                  inv.items.remove(k);
                  break;
                }
              }
            }
            else if (instructions[1].equals("Removed") && c.items.size() > id2){     
              ((Item)(c.items.get(id2))).inInventory = true;
              objMan.inv.items.add(c.items.get(id2));
              c.items.remove(id2);
            }
            break;
          }
        }
      }
      else if (infos[i].length() > 10 && infos[i].substring(0,10).equals("PlayerHP: ")){
        print(infos[i]);
        String[] instructions = splitTokens(infos[i].substring(10), " ");
        if (instructions[0].equals("Down")){
          ((Player)player).hp -= PApplet.parseFloat(instructions[1]);
        }
        else if (instructions[0].equals("Up")){
          ((Player)player).hp += PApplet.parseFloat(instructions[1]);
        }
      }
      else if (infos[i].length() > 10 && infos[i].substring(0,10).equals("Instance: ")){
        String[] instructions = splitTokens(infos[i].substring(10), " ");
        if (instructions[0].equals("Destroyed")){
          println(infos[i]);
          for (int j = 0; j < instances.size(); j++){
            if (instances.get(j).ID == PApplet.parseInt(instructions[1])){
              instances.remove(j);
              break;
            }
          }
        }
      }
    }
  }
}
class SupervisorViewer{
  public boolean focus;
  public boolean mouseWasJustPressed;
  public SupervisorViewer(){
    focus = true;
  }
  
  public float[] update(boolean[] keyHeld, float[] view, float[] bounds, float deltaTime){
    if (focus){
      if (keyHeld[0] && !keyHeld[1]){
        view[0] -= deltaTime*240;
        if (view[0] < 0) view[0] = 0;
      }
      else if (keyHeld[1] && !keyHeld[0]){
        view[0] += deltaTime*240;
        if (view[0] > bounds[0]-wid) view[0] = bounds[0]-wid;
      }
      if (keyHeld[2] && !keyHeld[3]){
        view[1] -= deltaTime*240;
        if (view[1] < 0) view[1] = 0;
      }
      else if (keyHeld[3] && !keyHeld[2]){
        view[1] += deltaTime*240;
        if (view[1] > bounds[1]-hei) view[1] = bounds[1]-hei;
      }
    }
    if (mousePressed && !mouseWasJustPressed){
      if (mouseY > (hei-32)*height/hei){
        focus = false;
        objMan.typStf.focus = true;
      }
      else{
        focus = true;
        objMan.typStf.focus = false;
        ArrayList<Instance> in = objMan.instances;
        for (int i = 0; i < in.size(); i++){
          if (in.get(i).x+in.get(i).w >= view[0] && in.get(i).y+in.get(i).h >= view[1] && in.get(i).x <= view[0]+wid && in.get(i).y <= view[1]+hei){
            if (in.get(i).selectable && mouseX >= (in.get(i).x-view[0])*width/wid && mouseX < (in.get(i).x+in.get(i).w-view[0])*width/wid 
                && mouseY >= (in.get(i).y-view[1])*height/hei && mouseY < (in.get(i).y+in.get(i).h-view[1])*height/hei){
                  addToLog(in.get(i).description+"\n ", new float[]{0,0,0}, false);
            }
          }
        }
      }
      mouseWasJustPressed = true;
    }
    else if (!mousePressed){
      mouseWasJustPressed = false;
    }
    return(view);
  }
  
  public void draw(float viewX, float viewY, Instance in){
    for (int i = 0; i < objMan.instances.size(); i++){
      if (in.selectable){
        if (mouseX >= (in.x-viewX)*width/wid && mouseX < (in.x+in.w-viewX)*width/wid && mouseY >= (in.y-viewY)*height/hei && mouseY < in.y+in.h-viewY*height/hei){
          if (mousePressed){
            stroke(200,255,200);
            strokeWeight(4);
          }
          else {
            stroke(0,255,0);
            strokeWeight(2);
          }
          rect((in.x-viewX)*width/wid, in.y-viewY*height/hei, in.w*width/wid, in.h*height/hei);
          noStroke();
        }
      }
    }
  }
}

class TypingStuff{
  String currentStr;
  public boolean focus;
  public TypingStuff(){
    focus = false;
    currentStr = "";
  }
  
  public void update(){
    if (focus){
      for (int i = 0; i < typed.size(); i++){
        if (typed.get(i) == '\n' && currentStr.length() > 0){
          addToLog(currentStr, new float[]{0,200,0});
          currentStr = "";
        }
        else if (typed.get(i) == '\b'){
          if (currentStr.length() > 0){
            currentStr = currentStr.substring(0, currentStr.length()-1);
          }
        }
        else{
          currentStr += typed.get(i);
        }
      }
    }
  }
  
  public void draw(){
    textSize(16*hei/height);
    if (focus) fill(255,255,255);
    else fill(200, 200, 200);
    stroke(0);
    strokeWeight(1);
    rect(0, (hei-32)*height/hei, width, 32*height/hei);
    noStroke();
    if (focus) fill(0,0,0);
    else fill(50, 50, 50);
    text(currentStr, 16*width/wid, (height-24)*height/hei);
    
  }
}

class Countdown extends Instance{
  boolean triggered;
  float countdown, visible;
  public Countdown(int X, int Y, int W, int H, float timer){
    super(X,Y);
    w = W*32;
    h = H*32;
    name = "Countdown";
    tag = "Boom";
    description = "Better start running fast!";
    selectable = false;
    countdown = timer;
  }
  
  public void update(Player p, float deltaTime){
    super.update(p, deltaTime);
    if (!triggered && x+dX*deltaTime < p.x+p.w+p.dX*deltaTime && x+w+dX*deltaTime > p.x+p.dX*deltaTime && y+dY*deltaTime < p.y+p.h+p.dY*deltaTime && y+h+dY*deltaTime > p.y+p.dY*deltaTime){
      
    }
  }
}
  public void settings() {  size(640,480); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "HackCUIII" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}

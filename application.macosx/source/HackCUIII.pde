import processing.net.*;

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
void setup(){
  size(640,480);
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
  //serv = new Server(this, 7575+gameMode);
  //clien = new Client(this, "127.0.0.1", 7575+gameMode);
  //clien = new Client(this, "127.0.0.1", 7575+((gameMode+1)%2));
}

void draw(){
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
  endTime = System.nanoTime()/1000000000.0;
  if (startTime == 0) startTime = endTime;
  float deltaTime = endTime-startTime;
  boolean[] kPress = new boolean[10];
  boolean[] kHeld = new boolean[10];
  for (int i = 0; i < 10; i++){
    kPress[i] = keyPress[i];
    kHeld[i] = keyHeld[i];
  }
  if (started) objMan.update(kPress, kHeld, deltaTime);
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

void keyPressed(){
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

void keyReleased(){
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

void keyReset(boolean[] prevKeyPressed){
  for (int i = 0; i < 10; i++){
    if (prevKeyPressed[i]){
      keyPress[i] = false;
    }
  }
}

void addToLog(String s, float[] col){
  addToLog(s, col, true);
}

void addToLog(String s, float[] col, boolean passToOther){
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

void logDraw(float deltaTime){
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

void drawIntroRoom(){
  fill(255);
  textSize(32*height/hei);
  text("Press Left to be the Player", width/8, height/2-32*height/hei);
  text("Press Right to be the Observer", width/8, height/2+32*height/hei);
}
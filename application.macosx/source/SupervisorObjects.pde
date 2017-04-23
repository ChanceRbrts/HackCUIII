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
                && mouseY >= (in.get(i).y-view[0])*height/hei && mouseY < (in.get(i).y+in.get(i).h-view[0])*height/hei){
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
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
    view[0] = x+w/2;
    view[1] = y+h/2;
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
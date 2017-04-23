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
          serv.write("PlayerHP: Down 5\n");
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
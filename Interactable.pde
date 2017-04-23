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
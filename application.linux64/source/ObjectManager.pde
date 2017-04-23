class ObjectManager{
  private float[] view, bounds;
  private float blackout, maxBlackout;
  private ArrayList<Instance> instances, instances2;
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
  
  void update(boolean[] keyPress, boolean[] keyHeld, float deltaTime){
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
        loadRoom(room);
        inv = new Inventory();
      }
    }
    if (gameMode == 1){
      for (int i = 0; i < instances2.size(); i++){
        instances2.get(i).update(player, deltaTime);
      }
      view = supView.update(keyHeld, view, bounds, deltaTime);
      typStf.update();
    }
  }
  
  void draw(){
    if (gameMode == 1){
      for (int i = 0; i < instances2.size(); i++){
        instances2.get(i).draw(view[0], view[1]);
       }
    }
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
  
  void loadRoom(int room){
    nextID = 0;
    instances = new ArrayList<Instance>();
    instances2 = new ArrayList<Instance>();
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
      if (room == 1){
        instances2.add(new Countdown(21,1,8,13,30));
      }
    }
  }
  
  void lookThroughServer(String info){
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
          player.x = float(positionMarker[0]);
          player.y = float(positionMarker[1]);
        }
      }
      else if (infos[i].length() > 22 && infos[i].substring(0,22).equals("PlayerInteractivePos: ")){
        String[] positionMarker = splitTokens(infos[i].substring(22), ",");
        if (positionMarker.length > 1){
          plaIn.x = float(positionMarker[0]);
          plaIn.y = float(positionMarker[1]);
        }
      }
      else if (infos[i].length() > 11 && infos[i].substring(0,11).equals("Inventory: ")){
        String[] instructions = splitTokens(infos[i].substring(11), " ");
        if (instructions[0].equals("Added")){
          int id = int(instructions[1]);
          for (int j = 0; j < instances.size(); j++){
            if (instances.get(j).ID == id){
              ((Item)instances.get(j)).inInventory = true;
              inv.items.add((Item)instances.get(j));
              break;
            }
          }
        }
        else if (instructions[0].equals("Dropped")){
          int id = int(instructions[1]);
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
        int id = int(instructions[0]);
        for (int j = 0; j < instances.size(); j++){
          if (instances.get(j).ID == id){
            Cabinet c = (Cabinet)(instances.get(j));
            int id2 = int(instructions[2]);
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
          ((Player)player).hp -= float(instructions[1]);
        }
        else if (instructions[0].equals("Up")){
          ((Player)player).hp += float(instructions[1]);
        }
      }
      else if (infos[i].length() > 10 && infos[i].substring(0,10).equals("Instance: ")){
        String[] instructions = splitTokens(infos[i].substring(10), " ");
        if (instructions[0].equals("Destroyed")){
          println(infos[i]);
          for (int j = 0; j < instances.size(); j++){
            if (instances.get(j).ID == int(instructions[1])){
              instances.remove(j);
              break;
            }
          }
        }
      }
    }
  }
}
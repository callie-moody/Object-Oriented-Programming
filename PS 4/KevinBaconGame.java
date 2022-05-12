import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class KevinBaconGame {

    //Current center of the universe
    public String center;
    //the overall graph of the universe
    public Graph<String, Set<String>> world= new AdjacencyMapGraph<String, Set<String>>();
    //the shortest path tree corresponding to the current universe center
    public Graph<String, Set<String>> shortestpaths= new AdjacencyMapGraph<String, Set<String>>();
    //Constructor
    public KevinBaconGame(){}

    //creates the universe graph by reading in the files
    public void createworld()
    {
        //stores the integer corresponding to each actor
        Map<Integer, String> actors= new HashMap<Integer,String>();
        //stores the integer corresponding to each movie
        Map<Integer, String> movies= new HashMap<Integer,String>();
        //stores the list of actors in each movie
        Map<String, Set<String>> movieactors= new HashMap<String,Set<String>>();;

        //initializes the readers for each file
        BufferedReader actornum=null;
        BufferedReader movienum=null;
        BufferedReader actormovie=null;
        try{
            actornum= new BufferedReader(new FileReader("inputs/actors.txt"));
            movienum= new BufferedReader(new FileReader("inputs/movies.txt"));
            actormovie= new BufferedReader(new FileReader("inputs/movie-actors.txt"));}
        catch(Exception e)
        { System.err.println("Could not open file"); }


    try {
        //read from the actor code file, splitting integer and corresponding actor into two strings
        String line = actornum.readLine();
        while(line!=null) {
            String[] code = line.split("\\|");
            //add corresponding values to the map
            actors.put(Integer.parseInt(code[0]), code[1]);
            line = actornum.readLine();}

        //read from the movie code file, splitting integer and corresponding actor into two strings
         line = movienum.readLine();
        while(line!=null) {
            String[] code = line.split("\\|");
            //add corresponding values to the map
            movies.put(Integer.parseInt(code[0]), code[1]);
            line = movienum.readLine(); }

        //add each actor as a vertex to the universe map
        for(String actor: actors.values())
            {world.insertVertex(actor);}

        //read from the actor-movie code file, splitting movie and corresponding actor into two strings
        line = actormovie.readLine();
        while(line!=null) {
            String[] code = line.split("\\|");
            String movie= movies.get(Integer.parseInt(code[0]));
            String actor= actors.get(Integer.parseInt(code[1]));
            //if the movie has already been entered into the map, add the actor to its actor list
            if(movieactors.containsKey(movie))
            {movieactors.get(movie).add(actor);}
            //if this is a new movies, put it into the map with a new list only containing this actor
            else
            {Set<String> newactor= new TreeSet<String>();
            newactor.add(actor);
            movieactors.put(movie, newactor); }

            line = actormovie.readLine();}

    } catch (Exception e) {
        System.err.println("reading error");
    }

    //for each movie
    for(String m: movieactors.keySet())
    {
        //for each actor in the list for that movie
        for(String s: movieactors.get(m))
        {
            for(String s2: movieactors.get(m))
            {   //do not draw an edge to yourself
                if(s.equals(s2))
                {}
                //if two actors are already connected, add the movie to the edge label
                else if(world.hasEdge(s,s2))
                { world.getLabel(s,s2).add(m); }
               //if the two actors have a new connection, draw the edge and label it with a new list only containing this movie
                else
                { Set<String> newedge= new TreeSet<String>();
                    newedge.add(m);
                    world.insertUndirected(s,s2,newedge); }
            }
        }
    }
    }


    //set center of universe and reset the shortest path tree to match
    public void setcenter(String name)
    {center=name;
    createpathtree();}

    //creates the shortest path tree
    public void createpathtree()
    {shortestpaths=KevinBaconGraph.bfs(world,center);}

    //finds degrees of separation, length of the shortest path
    public int degreessep(String s)
    { return KevinBaconGraph.getPath(shortestpaths,s).size()-1; }

    //finds number of actors connected to center in the whole network
    public int numinnetwork()
    {return shortestpaths.numVertices();}

    //average path length of all connected actors
    public double avgpath()
    {return KevinBaconGraph.averageSeparation(shortestpaths,center);}

    //New class that contains both the actor name and a score
    public static class ActorsBacons{
        double rating;
        String actorname;

        //constructor, stores name and score
        public ActorsBacons(String n, double s){
            actorname=n;
            rating=s; }

        //returns the score
        public double getscore()
        {return rating;}

        //to string method- just returns actor name
        @Override
        public String toString(){return actorname;}

    }
        //creates user interface, allows commands to be inputted, runs until user quits
        public static void menu()
        { //create a new game, automatically centered at kevin bacon
            KevinBaconGame game= new KevinBaconGame();
            game.createworld();
            game.setcenter("Kevin Bacon");

            //initialize scanner to accepts user commands
            Scanner inputuser= new Scanner(System.in);
            String input="";
            //loops over menu until user quits
            while(!input.equals("q")) {

                //Lists out options for commands and their functions
                System.out.println("Welcome to the " + game.center + " Game! What would you like to do?");
                System.out.println(game.numinnetwork() + " actors in the network of " + game.center);
                System.out.println();
                System.out.println("u. Set new universe center");
                System.out.println("d. list actors sorted by degree, with degree between inputted low and high values");
                System.out.println("c. list top or bottom centers of the universe, sorted by average separation");
                System.out.println("i. list actors with infinite separation from the current center");
                System.out.println("p. find path from an actor to current center of the universe");
                System.out.println("s. list actors sorted by non-infinite separation from the current center, " +
                        "with separation between low and high ");
                System.out.println("q. Quit Game");

                input = inputuser.nextLine();

                //sets new universe center
                if (input.equals("u")) {
                    System.out.println("Who is your new center?");
                    game.setcenter(inputuser.nextLine());}

                //lists actors sorted by degree, with min and max degree set by user
                else if (input.equals("d")) {
                    //stores current center to return to it
                    String currentcenter=game.center;

                    //set min and max
                    System.out.println("What is your min degree?");
                    int min = Integer.parseInt(inputuser.nextLine());
                    System.out.println("What is your max degree?");
                    int max = Integer.parseInt(inputuser.nextLine());

                    //create priority queue to sort
                    Queue<ActorsBacons> baconranked = new PriorityQueue<ActorsBacons>((b2, b1) -> Double.compare(b2.getscore(), b1.getscore()));

                    //restricts listed actors to only those in kevin bacon universe(no mini outside networks)
                    game.setcenter("Kevin Bacon");
                    //for each actor,find their degree on the world graph
                    // and add an ActorBacons object containing this degree to the priority queue
                    for (String a : game.shortestpaths.vertices()) {
                        game.setcenter(a);
                        int degree= game.world.inDegree(a);
                        //only adds if degree is within user parameters
                        if(degree>=min&&degree<=max){
                        baconranked.add(new ActorsBacons(a,degree));}
                    }
                    //prints out the actors in order of degree
                    System.out.println(baconranked);
                    //reset center to initial value
                    game.setcenter(currentcenter);
                }

                //list top or bottom centers of the universe, sorted by average separation
                else if (input.equals("c")) {
                    //stores current center to return to it
                    String currentcenter=game.center;

                    //takes user input for number of actors to rank, and whether best or worst should be ranked
                    System.out.println("How many actors do you want to rank? " +
                            "(positive number for the most closely related actors, negative for the least)");
                    int num = Integer.parseInt(inputuser.nextLine());
                    //creates priority queue
                    Queue<ActorsBacons> baconranked = new PriorityQueue<ActorsBacons>();

                    //if number is positive, ranks in order of best, if negative ranks in order of worst
                    if (num > 0) {
                        baconranked = new PriorityQueue<ActorsBacons>((b2, b1) -> Double.compare(b2.getscore(), b1.getscore())); }
                    else if (num < 0)
                    { baconranked = new PriorityQueue<ActorsBacons>((b2, b1) -> Double.compare(b1.getscore(), b2.getscore()));}

                    //restricts ranked actors to those in the kevin bacon universe
                    game.setcenter("Kevin Bacon");

                    //take each vertex, set as center to determine average path length,
                    // add ActorsBacons object to priority queue
                    for (String a : game.shortestpaths.vertices()) {
                    game.setcenter(a);
                    baconranked.add(new ActorsBacons(a, game.avgpath()));}

                    //only print out as many actors as the user requested
                    for(int i=0; i<Math.abs(num); i++) {
                    System.out.println(baconranked.remove()); }

                    //reset center to original center
                    game.setcenter(currentcenter);
                }

                //list actors with infinite separation from the current center (not connected)
                else if(input.equals("i")){
                    System.out.println(KevinBaconGraph.missingVertices(game.world,game.shortestpaths)); }

                //find path from inputted actor to current center of the universe
                else if(input.equals("p")){
                    //takes user input for chosen actor
                    System.out.println("Who do you want to find?");
                    String name=inputuser.nextLine();
                    //prints degrees of separation
                    System.out.println(game.degreessep(name) + " degrees of separation:");
                    //creates actual path list
                    List<String> path= KevinBaconGraph.getPath(game.shortestpaths, name);

                    //prints out path list, including the edge labels containing movies they appeared in together
                    for(int i=0; i<path.size()-1; i++) {
                        System.out.println(path.get(i) + " appeared in " +
                                game.world.getLabel(path.get(i), path.get(i+1)) + " with " + path.get(i+1)); } }

                //list actors sorted by non-infinite separation from the current center, with separation within user parameters
               else if(input.equals("s")){
                   //take user parameter input
                    System.out.println("What is your min distance?");
                    int min = Integer.parseInt(inputuser.nextLine());
                    System.out.println("What is your max distance?");
                    int max = Integer.parseInt(inputuser.nextLine());

                    //create priority queue to sort actors
                    Queue<ActorsBacons> baconranked = new PriorityQueue<ActorsBacons>
                            ((b2, b1) -> Double.compare(b2.getscore(), b1.getscore()));

                    //for every actor in the current network (not necessarily kevin bacon's) find distance to the center
                    for (String a : game.shortestpaths.vertices()) {
                        int degree= KevinBaconGraph.getPath(game.shortestpaths,a).size();
                        //create ActorsBacons object storing path length and add to priority queue (only if within parameters)
                        if(degree>=min&&degree<=max&&!a.equals(game.center)) {baconranked.add(new ActorsBacons(a,degree));}
                        }
                    System.out.println(baconranked);
                }
            }
        }



    public static void main(String[] args) {
        menu(); }
}


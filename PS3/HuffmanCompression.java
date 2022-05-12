import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class HuffmanCompression {

    public Map<Character, Integer> charfreq = new TreeMap<Character, Integer>();
    public BinaryTree<CharFreq> huff;
    public Map<Character, String> Convmap = new TreeMap<Character, String>();
    public PriorityQueue<BinaryTree<CharFreq>> queue = new PriorityQueue<BinaryTree<CharFreq>>(new TreeComparator());
    public String pathName = "inputs/WarAndPeace.txt";
    public String pathNameout = "inputs/WarAndPeace_compressed.txt";
    public String pathNamedone = "inputs/WarAndPeace_decompressed.txt";
    public boolean debug= false;

    public HuffmanCompression() {
    }
    //main method
    public static void main(String[] args) {

        HuffmanCompression obj = new HuffmanCompression();
        obj.fillcfmap();
        obj.makepq();

        //These methods throw exception if the file is empty
        try{obj.fillhuff();
        obj.fillconvmap();}
        catch(Exception e){
            System.err.println("File is Empty");
        }
        //optional debugging print lines
        if(obj.debug){System.out.println(obj.huff);
        System.out.println(obj.Convmap);}
        obj.compress();
        obj.decompress();


    }

    //Reads in the file and adds character to a map
    //increments frequency in the map as characters are read in
    public void fillcfmap() {
        //try to open the file
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(pathName));
        } catch (Exception e) {
            System.err.println("file not found");
        }
        //read in one character at a time
        try {
            int cInt = input.read(); // Read next character's integer representation
            while (cInt != -1) {
                char c = (char) cInt;
                //increment frequency based on character occurence
                if (!charfreq.containsKey(c)) {
                    charfreq.put(c, 1);
                } else {
                    charfreq.put(c, charfreq.get(c) + 1);
                }

                cInt = input.read(); // Read next character's integer representation
            }

        } catch (Exception e) {
            System.err.println("File reading error");
        }
        //Close the file
        finally {
            try {
                input.close();
            } catch (Exception e) {
                System.err.println("can't close file");
            }
        }

    }

    public void makepq() {//make binary trees of node CharFreq, enter into queue
        for (Character c : charfreq.keySet()) {//for each charfreq in the map, create new binary tree
            CharFreq temp = new CharFreq(charfreq.get(c), c);
            BinaryTree<CharFreq> entry = new BinaryTree<CharFreq>(temp);
            queue.add(entry);//add to the priority queue
        }
    }

    public void fillhuff()throws Exception {//combine trees until one is left, save as huffman
        while (queue.size() > 1) {
            BinaryTree<CharFreq> sub1 = queue.remove();
            BinaryTree<CharFreq> sub2 = queue.remove();
            Integer totalfreq = sub1.getData().getFreq() + sub2.getData().getFreq();
            BinaryTree<CharFreq> sup = new BinaryTree<CharFreq>(new CharFreq(totalfreq, null), sub1, sub2);
            queue.add(sup);
        }
        huff = queue.remove();

    }

    //Creates map of characters with binary representations
    public void fillconvmap() throws Exception
    {//run through huffman and enter paths to map with characters
        fillconvmaphelper(huff, "");
    }

    //recursively find the proper binary representations
    public void fillconvmaphelper(BinaryTree<CharFreq> tree, String path) throws Exception{

        //increment downwards, adding to the binary path string
        if (tree.hasLeft()) {
            String temp = path + "0";
            fillconvmaphelper(tree.getLeft(), temp);

        }

        if (tree.hasRight()) {
            String temp = path + "1";
            fillconvmaphelper(tree.getRight(), temp);
        }
        //When on a character, save binary path string
        if (tree.getData().getCharacter() != null && path.length()>0) {
            Convmap.put(tree.getData().getCharacter(), path);
        }
        //if this character is the only one, and therefore a node, creat a new tree with a null character head,
        // and this character as the left child
        //path will then be 0
        else if((tree.getData().getCharacter() != null && path.length()==0))
        {path="0";
            BinaryTree<CharFreq> newtree= new BinaryTree<CharFreq>(new CharFreq(0,null),tree, null);
            Convmap.put(tree.getData().getCharacter(), path);
            huff=newtree;
        }
    }

    public void compress() {//uses conversion map to read in individual characters,
        // convert them, and then immediately print out binary

        //read in the file
        BufferedReader input = null;
        BufferedBitWriter output = null;

        try {
            input = new BufferedReader(new FileReader(pathName));
            output = new BufferedBitWriter(pathNameout);
        } catch (Exception e) {
            System.err.println("file not found");
        }

        try {
            int cInt = input.read(); // Read next character's integer representation
            while (cInt != -1) {
                Character character = (char) cInt;
                //get bit string
                String bits = Convmap.get(character);
                //convert bit string to written binary
                for (int i = 0; i < bits.length(); i++) {
                    if (bits.charAt(i) == '0') {
                        output.writeBit(false);
                    } else {
                        output.writeBit(true);
                    }

                }
                cInt = input.read(); // Read next character's integer representation
            }

        } catch (Exception e) {
            System.err.println("File reading/writing error");
        }
        //close the file
        finally {
            try {
                input.close();
                output.close();
            } catch (Exception e) {
                System.err.println("can't close file");
            }
        }


    }

    public void decompress() {//takes in the bits from the file, traces path down huffman tree, and then converts to character.

        //read in the file
        BufferedBitReader input = null;
        BufferedWriter output = null;

        try {
            input = new BufferedBitReader(pathNameout);
            output = new BufferedWriter(new FileWriter(pathNamedone));
        } catch (Exception e) {
            System.err.println("file not found");
        }

        try {
            boolean cInt = input.readBit(); // Read next character's integer representation

            BinaryTree<CharFreq> find = huff;

            while (input.hasNext()) {

            //for each bit, go down either left or right one
                if (!cInt) {
                    find = find.getLeft();
                } else {
                    find = find.getRight();
                }
                //when at the bottom of the tree, write out character value
                if (find.isLeaf()) {
                    output.write(find.getData().getCharacter());
                    find = huff;
                }
                cInt = input.readBit();
            }
            if (!cInt) {
                find = find.getLeft();
            } else {
                find = find.getRight();
            }
            //when at the bottom of the tree, write out character value
            if (find.isLeaf()) {
                output.write(find.getData().getCharacter());
                find = huff;
            }
        } catch (Exception e) {
            System.err.println("cannot write to file");
        }
        //close the file
        finally {
            try {
                input.close();
                output.close();
            } catch (Exception e) {
                System.err.println("can't close file");
            }
        }


    }

    //Binary tree comparator class
    public class TreeComparator implements Comparator<BinaryTree<CharFreq>> {


        public TreeComparator() {
        }
        //compares frequencies of CharFreq data
        public int compare(BinaryTree<CharFreq> cf1, BinaryTree<CharFreq> cf2) {
            if (cf1.getData().getFreq() - cf2.getData().getFreq() > 0) {
                return 1;
            } else if (cf1.getData().getFreq() - cf2.getData().getFreq() == 0) {
                return 0;
            } else {
                return -1;
            }
        }

    }

    //object that can hold character and frequency
    protected class CharFreq {
        protected Integer freq;
        protected Character character;

        protected CharFreq(Integer f, Character c) {
            freq = f;
            character = c;
        }

        protected Integer getFreq() {
            return freq;
        }

        protected Character getCharacter() {
            return character;
        }

        @Override
        public String toString() {
            return "Character= " + character + " Frequency= " + freq;
        }
    }
}


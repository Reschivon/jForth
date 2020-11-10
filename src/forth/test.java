package forth;

import java.util.ArrayList;
import java.util.Scanner;

public class test {
    public static void main(String[] args) {
        /*ArrayList<Byte> b= new ArrayList<>();
        Scanner scan = new Scanner(System.in);
        while(scan.hasNext()){
            int m = Integer.parseInt(scan.next().replace(',',' ').strip());
            b.add((byte) (m>128?0:m));
            System.out.println("r");
        }
        System.out.println("v");
        Byte[] h = new Byte[b.size()];
        b.toArray(h);
        System.out.println("l");*/
        read_string(new byte[]{-1, 9, 115, 101, 101, 115, 116, 97, 99, 107, 0, 1, 7, 115, 101, 101, 109, 101, 109, 0, 12, 10, 115, 101, 101, 114, 97, 119, 109, 101, 109, 0, 21, 10, 108, 97, 115, 116, 105, 110, 109, 101, 109, 0, 33, 6, 112, 117, 115, 104, 49, 0, 45, 6, 112, 114, 105, 110, 116, 0, 53, 7, 114, 101, 116, 117, 114, 110, 1, 61, 12, 116, 111, 107, 101, 110, 62, 115, 116, 97, 99, 107, 0, 70, 10, 115, 116, 97, 99, 107, 62, 109, 101, 109, 0, 84, 2, 91, 1, 96, 2, 93, 0, 100, 8, 108, 105, 116, 101, 114, 97, 108, 0, 104, 4, 108, 105, 116, 0, 114, 10, 110, 101, 120, 116, 45, 119, 111, 114, 100, 0, 120, 12, 114, 101, 97, 100, 45, 115, 116, 114, 105, 110, 103, 0, 0, 9, 114, 101, 97, 100, 45, 109, 101, 109, 0, 0, 7, 99, 114, 101, 97, 116, 101, 0, 0, 23, 99, 111, 112, 121, 45, 115, 116, 114, 105, 110, 103, 45, 102, 114, 111, 109, 45, 115, 116, 97, 99, 107, 0, 0, 2, 58, 0, 0, 120, 0, 21, 104, 0, 84, 100, 61, 0, 2, 59, 1, 96, 104, 61, 84, 12, 61, 0, 61, 0, 7, 100, 111, 117, 98, 108, 101, 7, 100, 111, 117, 98, 108, 101
        });
    }
    static void read_string(Byte[] str) {
        byte[] o = new byte[str.length];
        int i=0;for(byte t:str){
            o[i++] = t;
        }
        System.out.println(new String(o));
    }
    static void read_string(byte[] str) {

        System.out.println(new String(str));
    }

}

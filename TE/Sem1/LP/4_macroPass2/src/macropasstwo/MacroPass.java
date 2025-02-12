package macropasstwo;

import java.io.*;

public class MacroPass {
    public static void main(String[] args) throws IOException {
        Mdt[] Mdt = new Mdt[20];
        Mnt[] MNT = new Mnt[4];
        Arglist[] formal_parameter = new Arglist[10];
        int macro_addr = -1;

        boolean macro_start = false, macro_end = false;
        int macro_call = -1;
        int Mdt_cnt = 0, mnt_cnt = 0, formal_Arglist_cnt = 0, actual_Arglist_cnt = 0, temp_cnt = 0, temp_cnt1 = 0;

        BufferedReader br1 = new BufferedReader(new FileReader("src\\macropasstwo\\Mnt.txt"));
        String line;
        while ((line = br1.readLine()) != null) {
            String[] parts = line.split("\\s+");
            MNT[mnt_cnt++] = new Mnt(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }
        br1.close();
        System.out.println("\n\t********MACRO NAME TABLE**********");
        System.out.println("\n\tINDEX\tNAME\tADDRESS\tTOTAL ARGUMENTS");
        for (int i = 0; i < mnt_cnt; i++)
            System.out.println("\t" + i + "\t\t" + MNT[i].name + "\t\t" + MNT[i].addr + "\t\t" + MNT[i].arg_cnt);

        br1 = new BufferedReader(new FileReader("src\\macropasstwo\\Arglist.txt"));
        while ((line = br1.readLine()) != null) {
            String[] parameters = line.split("\\s+");
            formal_parameter[formal_Arglist_cnt++] = new Arglist(parameters[0]);
            if (parameters.length > 1)
                formal_parameter[formal_Arglist_cnt - 1].value = parameters[1];
        }
        br1.close();

        System.out.println("\n\n\t********FORMAL ARGUMENT LIST**********");
        System.out.println("\n\tINDEX\tNAME\tADDRESS");
        for (int i = 0; i < formal_Arglist_cnt; i++)
            System.out.println("\t" + i + "\t\t" + formal_parameter[i].argname + "\t" + formal_parameter[i].value);

        br1 = new BufferedReader(new FileReader("src\\macropasstwo\\Mdt.txt"));
        while ((line = br1.readLine()) != null) {
            Mdt[Mdt_cnt] = new Mdt();
            Mdt[Mdt_cnt++].st = line;
        }
        br1.close();

        System.out.println("\n\t********MACRO DEFINITION TABLE**********");
        System.out.println("\n\tINDEX\t\tSTATEMENT");
        for (int i = 0; i < Mdt_cnt; i++)
            System.out.println("\t" + i + "\t" + Mdt[i].st);

        br1 = new BufferedReader(new FileReader("src\\macropasstwo\\input.txt"));
        Arglist[] actual_parameter = new Arglist[10];
        BufferedWriter bw1 = new BufferedWriter(new FileWriter("src\\macropasstwo\\Output.txt"));
        while ((line = br1.readLine()) != null) {
            line = line.replaceAll(",", " ");
            String[] tokens = line.split("\\s+");
            temp_cnt1 = 0;
            for (String current_token : tokens) {
                if (current_token.equalsIgnoreCase("macro")) {
                    macro_start = true;
                    macro_end = false;
                }
                if (macro_end && !macro_start) {
                    if (macro_call != -1 && temp_cnt < formal_Arglist_cnt - 1) {
                        if (!formal_parameter[actual_Arglist_cnt].value.isEmpty())
                            actual_parameter[actual_Arglist_cnt++] = new Arglist(formal_parameter[actual_Arglist_cnt - 1].value);

                        actual_parameter[actual_Arglist_cnt++] = new Arglist(current_token);

                        if (!formal_parameter[actual_Arglist_cnt].value.isEmpty())
                            actual_parameter[actual_Arglist_cnt++] = new Arglist(formal_parameter[actual_Arglist_cnt - 1].value);

                    }

                    for (int i = 0; i < mnt_cnt; i++) {
                        if (current_token.equals(MNT[i].name)) {
                            macro_call = i;
                            temp_cnt1 += MNT[i].arg_cnt;
                            break;
                        }
                        temp_cnt1 += MNT[i].arg_cnt;
                    }
                    if (macro_call == -1)
                        bw1.write("\t" + current_token);
                }
                if (current_token.equalsIgnoreCase("mend")) {
                    macro_end = true;
                    macro_start = false;
                }
            }
            if (macro_call != -1) {
                macro_addr = MNT[macro_call].addr + 1;
                while (true) {
                    if (Mdt[macro_addr].st.contains("mend") || Mdt[macro_addr].st.contains("MEND")) {
                        macro_call = -1;
                        break;
                    } else {
                        bw1.write("\n");
                        String[] temp_tokens = Mdt[macro_addr++].st.split("\\s+");

                        for (String temp : temp_tokens) {
                            if (temp.matches("#[0-9]+") || temp.matches(",#[0-9]+")) {
                                int num = Integer.parseInt(temp.replaceAll("[^0-9]+", ""));
                                bw1.write(actual_parameter[num - 1].argname + "\t");
                            } else
                                bw1.write(temp + "\t");
                        }
                    }
                }
            }
            if (!macro_start)
                bw1.write("\n");
            macro_call = -1;
        }
        br1.close();
        bw1.close();

        System.out.println("\n\n\t********ACTUAL ARGUMENT LIST**********");
        System.out.println("\n\tINDEX\tNAME");
        for (int i = 0; i < actual_Arglist_cnt; i++)
            System.out.println("\t" + i + "\t" + actual_parameter[i].argname);
    }
}



/*********MACRO NAME TABLE**********

INDEX	NAME	ADDRESS	  TOTAL ARGUMENTS
0		INCR		0		3
1		DECR		5		3


********FORMAL ARGUMENT LIST**********

INDEX	NAME	ADDRESS
0		&X	
1		&Y	
2		&REG1	AREG
3		&A	
4		&B	
5		&REG2	BREG

********MACRO DEFINITION TABLE**********

INDEX	STATEMENT
0		INCR	&X	,&Y	,&REG1	=	AREG
1		MOVER	#3	,#1
2		ADD	#3	,#2
3		MOVEM	#3	,#1
4		MEND
5		DECR	&A	,&B	,&REG2	=	BREG
6		MOVER	#6	,#4
7		SUB	#6	,#5
8		MOVEM	#6	,#4
9		MEND


********ACTUAL ARGUMENT LIST**********

  INDEX	NAME
	0	N1
	1	N2
	2	AREG
	3	N1
	4	N2
	5	BREG
*/

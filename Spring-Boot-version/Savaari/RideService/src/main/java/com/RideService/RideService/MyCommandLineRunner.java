package com.RideService.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.*;
import java.io.*;
@Component
public class MyCommandLineRunner implements CommandLineRunner {
    private final CabtbRepository cabrep;
    private final DummyRepository dummyRep;
    static ArrayList<Long> cabs,custs,balances;

    @Autowired
    public MyCommandLineRunner(CabtbRepository cabrep, DummyRepository dummyRep) {
        this.cabrep = cabrep;
        this.dummyRep = dummyRep;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Hello world");
        // Student student1 = new Student("foo", "bar", "foobar@iisc.ac.in");
        // Cabtb c = new Cabtb(120);
        // cabrep.saveAndFlush(c);
        Dummy d = new Dummy();
        dummyRep.saveAndFlush(d);

        takeInput();
        


        for(int i=0;i<cabs.size();++i){
            Cabtb c = new Cabtb(cabs.get(i),-1L);
            cabrep.saveAndFlush(c);
        }



        
    }
    static void takeInput() throws Exception{
        Scanner sc = new Scanner(new File("IDs.txt"));
        String line = sc.nextLine();
        line  = sc.nextLine();
        custs = new ArrayList<>();
        balances = new ArrayList<>();
        cabs = new ArrayList<>();
        while(!line.contains("*")){
            cabs.add(Long.parseLong(line));
            line = sc.nextLine();
        }
        line = sc.nextLine();

        while(!line.contains("*")){
            custs.add(Long.parseLong(line));
            line = sc.nextLine();
        }

        line = sc.nextLine();
        long bal = Long.parseLong(line);
        for(int i=0;i<custs.size();++i){
            balances.add(bal);
        }
       
    }
}
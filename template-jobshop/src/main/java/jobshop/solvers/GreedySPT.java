package jobshop.solvers;

import com.sun.org.apache.xml.internal.utils.SystemIDResolver;
import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.Iterator;
import java.util.Vector;

public class GreedySPT implements Solver {



    @Override
    public Result solve(Instance instance, long deadline){

        Vector<Task> realisable = new Vector<Task>();
        ResourceOrder sol = new ResourceOrder(instance);

        //init -- add first task of each job
        for(int j = 0 ; j<instance.numJobs ; j++) {
            realisable.add(new Task(j,0));
        }

        int remainingTasks = instance.numJobs*instance.numMachines ;
        int machine;
        while(remainingTasks>0) { //change for Tant que taches réalisables
            Task current = spt(realisable,instance);
            machine = instance.machine(current);
            sol.tasksByMachine[machine][sol.nextFreeSlot[machine]] = current;
            sol.nextFreeSlot[machine]++;
            if(current.task + 1 < instance.numMachines){
                realisable.add(new Task(current.job, current.task+1));
            }
            realisable.remove(current);
            remainingTasks--;
        }
        //System.out.println(sol);
        return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
    }

    private Task spt(Vector<Task> tasks, Instance instance){
        Task current = tasks.firstElement();
        Task result = current;
        for(int i = 1; i< tasks.size(); i++){
            current = tasks.elementAt(i);
            if (instance.duration(current) < instance.duration(result)) result = current ;
        }
        //System.out.println("Best result " + result);
        return result ;
    }
}

package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.Iterator;
import java.util.Vector;

public class GreedyESTSPT implements Solver {

    @Override
    public Result solve(Instance instance, long deadline){
        Vector<Task> realisable = new Vector<Task>();
        ResourceOrder sol = new ResourceOrder(instance);
        int[] estMachine = new int[instance.numMachines];
        int[] estJob = new int[instance.numJobs];

        //init -- add first task of each job
        for (int j = 0; j < instance.numJobs; j++) {
            realisable.add(new Task(j, 0));
            estMachine[instance.machine(j, 0)] = 0;
            estJob[j] = 0;
        }

        int remainingTasks = instance.numJobs * instance.numMachines;
        int machine;
        Task current;
        Task nextTask;
        Vector<Task> estTasks;
        int start ;
        while (remainingTasks > 0) { // Tant que taches réalisables
            current = spt(realisable, estMachine, estJob,instance) ;
            machine = instance.machine(current);
            sol.tasksByMachine[machine][sol.nextFreeSlot[machine]] = current;
            sol.nextFreeSlot[machine]++;
            if (current.task + 1 < instance.numMachines) {
                nextTask = new Task(current.job, current.task + 1);
                realisable.add(nextTask);
                //System.out.println("estMachine :\t" + estMachine + "estJob :\t" + estJob);
            }
            realisable.remove(current);
            start = Math.max(estJob[current.job], estMachine[machine]) + instance.duration(current);
            estMachine[machine] = start;
            estJob[current.job] = start;

            remainingTasks--;
        }
        //System.out.println(sol);
        return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
    }

    private Task spt(Vector<Task> tasks, int[] estMachine, int[] estJob, Instance instance){
        Iterator<Task> it = tasks.iterator();
        Task current = it.next();
        Task result = current;
        while(it.hasNext()){
            current = it.next();
            if(Math.max(estJob[current.job],estMachine[instance.machine(current)]) < Math.max(estJob[result.job],estMachine[instance.machine(result)])){
                result = current;
            }else if(Math.max(estJob[current.job],estMachine[instance.machine(current)]) == Math.max(estJob[result.job],estMachine[instance.machine(result)])){
                if (instance.duration(current) < instance.duration(result)) result = current ;
            }
        }
        return result ;
    }
}

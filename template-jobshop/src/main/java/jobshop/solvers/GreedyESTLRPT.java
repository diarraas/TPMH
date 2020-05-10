package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.Vector;

public class GreedyESTLRPT implements Solver {


    public Result solve(Instance instance, long deadline) {

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
            current = lrpt(realisable, estMachine, estJob,instance) ;
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

    private int totalJobDuration(Instance instance, Task t) {
        int result = 0;
        for (int i = t.task; i < instance.numTasks; i++) {
            result = result + instance.duration(t.job, i);
        }

        return result;
    }

    private Task lrpt(Vector<Task> tasks, int[] estMachine,int[] estJob, Instance instance) {
        Task current = tasks.firstElement();
        Task result = current;
        int currentEst ;
        int bestEst = Math.max(estJob[current.job], estMachine[instance.machine(current)]);;
        for(int i = 1; i<tasks.size();i++) {
            current = tasks.get(i);
            currentEst = Math.max(estJob[current.job], estMachine[instance.machine(current)]);
            if (currentEst < bestEst || (currentEst == bestEst) && totalJobDuration(instance, current) > totalJobDuration(instance, result)) {
                result = current;
                bestEst = currentEst;
            }
        }
        //System.out.println("Best result " + result);
        return result;

    }

    private void printTable(int[] t){
        System.out.print("[");
        for(int i : t)          System.out.print(i+"\t");;
        System.out.println("]");

    }
}
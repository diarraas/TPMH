package jobshop.solvers;
import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;
import java.util.Vector;

public class GreedyLRPT implements Solver {


    @Override
    public Result solve(Instance instance, long deadline){

        Vector<Task> realisable = new Vector<Task>();
        ResourceOrder sol = new ResourceOrder(instance);
        int[] nextTask = new int[instance.numMachines];

        //init -- add first task of each job
        for(int j = 0 ; j<instance.numJobs ; j++) {
            realisable.add(new Task(j,0));
        }

        int remainingTasks = instance.numJobs*instance.numMachines ;
        int machine;
        while(remainingTasks>0) { //change for Tant que taches réalisables
            Task current = lrpt(realisable,instance);
            machine = instance.machine(current);
            sol.tasksByMachine[machine][nextTask[machine]] = current;
            nextTask[machine]++;
            if(current.task + 1 < instance.numMachines){
                realisable.add(new Task(current.job, current.task+1));
            }
            realisable.remove(current);
            remainingTasks--;
        }
        //System.out.println(sol);
        return new Result(instance, sol.toSchedule(), Result.ExitCause.Blocked);
    }

    private int totalJobDuration(Instance instance, Task t){
        int result = 0 ;
        for(int i = t.task; i< instance.numTasks; i++){
            result = result + instance.duration(t.job,i);
        }

        return result ;
    }
    private Task lrpt(Vector<Task> tasks, Instance instance){
        Task current = tasks.firstElement();
        Task result = current;
        for(int i = 1; i< tasks.size(); i++){
            current = tasks.elementAt(i);
            if (totalJobDuration(instance, current) > totalJobDuration(instance, result))
                    result = current ;
        }
        //System.out.println("Best result " + result);
        return result ;
    }
}
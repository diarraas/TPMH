package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class TabooSolver implements Solver {

    static class Block {
        /** machine on which the block is identified */
        final int machine;
        /** index of the first task of the block */
        final int firstTask;
        /** index of the last task of the block */
        final int lastTask;

        Block(int machine, int firstTask, int lastTask) {
            this.machine = machine;
            this.firstTask = firstTask;
            this.lastTask = lastTask;
        }

        public String toString(){
            return "( m : " + machine + "\t first: " + firstTask +  "\t last: " + lastTask + " )\n";
        }
    }

    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order) {
            Task temp = order.tasksByMachine[this.machine][this.t1];
            order.tasksByMachine[this.machine][this.t1] = order.tasksByMachine[this.machine][this.t2];
            order.tasksByMachine[this.machine][this.t2] = temp;
        }
    }

    public Result solve(Instance instance, long deadline) {
        //init
        GreedyESTLRPT greedy = new GreedyESTLRPT();
        Result greedyResult = greedy.solve(instance, deadline);
        ResourceOrder currentOrder = new ResourceOrder(greedyResult.schedule);
        ResourceOrder bestOrder = currentOrder.copy();
        ResourceOrder temp;
        List<Block> criticalBlocks;
        List<Swap> neighboors;
        int k = 0;
        int kBest = 0 ;
        int maxBest = 50;
        int maxIter = 3000;
        int kOK = 4;
        int[][] dureeTaboo = new int[instance.numTasks*instance.numJobs][instance.numTasks*instance.numJobs];
        //Boucle principale
        while ((deadline - System.currentTimeMillis() > 1) && k < maxIter && kBest < maxBest) {
            k++;
           // System.out.println("current solution makespan" + currentOrder.toSchedule().makespan() );
            criticalBlocks = blocksOfCriticalPath(currentOrder);
            //Liste des voisins de chaque block
            for (Block block : criticalBlocks) {
                neighboors = neighbors(block);
                for (Swap neighboor : neighboors) {
                    temp = currentOrder.copy();
                    if (dureeTaboo[neighboor.t1][neighboor.t2] <= k){
                        neighboor.applyOn(temp);
                        currentOrder = temp;
                        //System.out.println("current " + currentOrder.toSchedule().makespan() );
                        if (currentOrder.toSchedule().makespan() < bestOrder.toSchedule().makespan()) {
                            bestOrder = currentOrder;
                            kBest = 0;
                        }else{
                            kBest++;
                        }
                        dureeTaboo[neighboor.t2][neighboor.t1]= k + kOK;
                    }
                }
            }
        }
      //  System.out.println("BEST solution makespan" + bestOrder.toSchedule().makespan() );

        return new Result(instance, bestOrder.toSchedule(), Result.ExitCause.Blocked);
    }

    private String printtab(int[][] tab, int max){
        String result = "" ;
        for(int i = 0; i< max; i++){
            for(int j = 0; j<max;j++)   result = result+ tab[i][j] + "\t";
            result = result+ "\n";

        }
        return result;
    }


    List<Block> blocksOfCriticalPath(ResourceOrder order) {
        List<Task> crititalPath = order.toSchedule().criticalPath();
//   System.out.println("CRITICAL PATH :" +crititalPath);
        List<Block> blocksofpath = new ArrayList<Block>();
        Task current, next ;
        for(int i = 0; i<crititalPath.size()-1;i++){
            current = crititalPath.get(i);
            next = crititalPath.get(i+1);
            if(blocksofpath.size() != 0  && blocksofpath.get(blocksofpath.size()-1).machine == order.instance.machine(current)) {
                Block newBlock = new Block(order.instance.machine(current), blocksofpath.get(blocksofpath.size() - 1).firstTask, taskIndex(order, current));
                blocksofpath.remove(blocksofpath.size() - 1);
                blocksofpath.add(newBlock);
            }else{
                if(order.instance.machine(current) == order.instance.machine(next)){
                    blocksofpath.add(new Block(order.instance.machine(current), taskIndex(order,current),taskIndex(order,next)));
                }
            }
        }
        current = crititalPath.get(crititalPath.size()-1);
        //System.out.println("BLOCK OF CRITICAL PATH :" +blocksofpath);
        return blocksofpath;
    }

    private int taskIndex(ResourceOrder order, Task t){
        int i = 0 ;
        while(i<order.instance.numJobs && !(order.tasksByMachine[order.instance.machine(t)][i]).equals(t))
            i++;
        if (i != order.instance.numJobs) return i;
        else throw new UnsupportedOperationException("Unknown Task");
    }
    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {
        List<Swap> neighboors = new ArrayList<Swap>();
        if(block.lastTask-block.firstTask == 1){
            Swap newSwap = new Swap(block.machine, block.firstTask, block.lastTask);
            neighboors.add(newSwap);
        }else if(block.lastTask-block.firstTask>1){
            Swap newSwap1 = new Swap(block.machine, block.firstTask, block.firstTask+1);
            Swap newSwap2 = new Swap(block.machine, block.lastTask-1, block.lastTask);
            neighboors.add(newSwap1);
            neighboors.add(newSwap2);
        }
        return neighboors;
    }


}

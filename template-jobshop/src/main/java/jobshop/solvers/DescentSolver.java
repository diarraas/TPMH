package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.List;

public class DescentSolver implements Solver {

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
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

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swam with : machine = 1, t1= 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
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


    @Override
    public Result solve(Instance instance, long deadline) {
        //init
        GreedyESTLRPT greedy = new GreedyESTLRPT();
        Result greedyResult = greedy.solve(instance, deadline);
        ResourceOrder currentOrder = new ResourceOrder(greedyResult.schedule);
        ResourceOrder init = currentOrder.copy();
        ResourceOrder temp;
        List<Block>  criticalBlocks;
        List<Swap> neighboors ;
        boolean changed = true;
        //Boucle principale
        while(changed && (deadline - System.currentTimeMillis() > 1)) {
            criticalBlocks = blocksOfCriticalPath(currentOrder);
            //Liste des voisins de chaque block
            for(Block block : criticalBlocks){
                neighboors = neighbors(block) ;
                for(Swap neighboor : neighboors){
                    temp = currentOrder.copy();
                    neighboor.applyOn(temp);
                    if(temp.toSchedule().makespan() < currentOrder.toSchedule().makespan()) {
                        currentOrder = temp;
                    }
                }
            }
            if (currentOrder.equals(init)){
                changed = false;
            }else {
                changed = true ;
                init = currentOrder;
            }
        }
        return new Result(instance, currentOrder.toSchedule(), Result.ExitCause.Blocked);
    }

    /** Returns a list of all blocks of the critical path. */
    List<Block> blocksOfCriticalPath(ResourceOrder order) {
        List<Task> crititalPath = order.toSchedule().criticalPath();
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

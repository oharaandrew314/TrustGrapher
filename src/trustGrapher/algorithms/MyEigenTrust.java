////////////////////////////////MyEigenTrust//////////////////////////////////
package trustGrapher.algorithms;

import cu.repsystestbed.algorithms.EigenTrust;

/**
 * An extension of EigenTrust to make it easier to do what I want it to!
 * @author Andrew O'Hara
 */
public class MyEigenTrust extends EigenTrust {

//////////////////////////////////Constructor///////////////////////////////////
    public MyEigenTrust(int iterations, double threshold2Satisfy) {
        super(iterations, threshold2Satisfy);
    }
///////////////////////////////////Methods//////////////////////////////////////

    public void setMatrixFilled(boolean filled){
        this.matrixFilled = filled;
    }
}
////////////////////////////////////////////////////////////////////////////////

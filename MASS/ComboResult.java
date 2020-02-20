/**
* Managing Neural Networking evaluation results on test data.
*/
package train_nn.mass_train_nn;

// * I/O imports
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class ComboResult implements Comparable<ComboResult>, Serializable {
    private static final long serialVersionUID = 2051995;
    int comboIndex;
    double accuracy;

    public ComboResult(
        int comboIndex,
        double accuracy
    ) {
        this.comboIndex = comboIndex;
        this.accuracy = accuracy;
    }

    @Override
    public int compareTo(ComboResult other)
    {
        if (this.accuracy <= other.accuracy) {
            return 1;
        }
        else {
            return -1;
        }
    }
}

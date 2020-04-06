package act.ring.cncert.data.nlp.utils;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.StringTokenizer;

/**
 * Imported from ring-event, 2017/11/08.
 */
public class SVMPredict {

    private svm_model SVM_Model;

    public SVMPredict(String model_filepath) {
        try {
            SVM_Model = svm.svm_load_model(model_filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double atof(String s) {
        return Double.valueOf(s).doubleValue();
    }

    private static int atoi(String s) {
        return Integer.parseInt(s);
    }

    private static void exit_with_help() {
        System.err.print("usage: SVMPredict [options] test_file model_file output_file\n"
                         + "options:\n"
                         + "-b probability_estimates: whether to predict probability estimates, 0 or 1 (default 0); one-class SVM not supported yet\n");
        System.exit(1);
    }

    private double predict(BufferedReader input, DataOutputStream output, svm_model model,
                           int predict_probability, boolean returnV) throws IOException {
        int correct = 0;
        int total = 0;
        double error = 0;
        double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;

        int svm_type = svm.svm_get_svm_type(model);
        int nr_class = svm.svm_get_nr_class(model);
        double[] prob_estimates = null;

        int
            TotalEventCounter
            [
            ] =
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
             0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int
            PCounter
            [
            ] =
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
             0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int
            TPcounter
            [
            ] =
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
             0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        if (predict_probability == 1) {
            if (svm_type == svm_parameter.EPSILON_SVR ||
                svm_type == svm_parameter.NU_SVR) {
                System.out.print(
                    "Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="
                    + svm.svm_get_svr_probability(model) + "\n");
            } else {
                int[] labels = new int[nr_class];
                svm.svm_get_labels(model, labels);
                prob_estimates = new double[nr_class];
                output.writeBytes("labels");
                for (int j = 0; j < nr_class; j++) {
                    output.writeBytes(" " + labels[j]);
                }
                output.writeBytes("\n");
            }
        }
        double accuracy = 0;
        while (true) {
            String line = input.readLine();
            if (line == null) {
                break;
            }

            StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

            double target = atof(st.nextToken());
            int m = st.countTokens() / 2;
            svm_node[] x = new svm_node[m];
            for (int j = 0; j < m; j++) {
                x[j] = new svm_node();
                x[j].index = atoi(st.nextToken());
                x[j].value = atof(st.nextToken());
            }

            double v;
            if (predict_probability == 1 && (svm_type == svm_parameter.C_SVC
                                             || svm_type == svm_parameter.NU_SVC)) {
                v = svm.svm_predict_probability(model, x, prob_estimates);
                output.writeBytes(v + " ");
                for (int j = 0; j < nr_class; j++) {
                    output.writeBytes(prob_estimates[j] + " ");
                }
                output.writeBytes("\n");
            } else {
                v = svm.svm_predict(model, x);
                output.writeBytes("v = " + v + "target = " + target + "\n");
            }
            int index = (int) Double.parseDouble(target + "");
            PCounter[index]++;
            if (v == target) {
                TPcounter[index]++;
            }
            index = (int) Double.parseDouble(v + "");
            TotalEventCounter[index]++;

            if (v == target) {
                ++correct;
            }
            error += (v - target) * (v - target);
            sumv += v;
            sumy += target;
            sumvv += v * v;
            sumyy += target * target;
            sumvy += v * target;
            ++total;
            accuracy = v;
        }
        if (svm_type == svm_parameter.EPSILON_SVR ||
            svm_type == svm_parameter.NU_SVR) {
            System.out.print("Mean squared error = " + error / total + " (regression)\n");
            System.out.print("Squared correlation coefficient = " +
                             ((total * sumvy - sumv * sumy) * (total * sumvy - sumv * sumy)) /
                             ((total * sumvv - sumv * sumv) * (total * sumyy - sumy * sumy)) +
                             " (regression)\n");
        } else {
            if (!returnV) {
                System.out.print("Accuracy = " + (double) correct / total * 100 +
                                 "% (" + correct + "/" + total + ") (classification)\n");

                for (int i = 1; i <= 9; i++) {
                    if (i == 5) {
                        continue;
                    }
                    System.out.println(
                        "Type " + i + ": (TP)=" + TPcounter[i] + "; There should be (P=TP+FN)"
                        + PCounter[i] + "tags;"
                        + " The outcome contains (TP+FP)" + TotalEventCounter[i] + "tags of type "
                        + i + "   :   Precision = "
                        + (float) TPcounter[i] / TotalEventCounter[i] + "  Recall = "
                        + (float) TPcounter[i] / PCounter[i]);
                }
                accuracy = (double) correct / total;
            }
        }
        return accuracy;
    }

    public double predict(String inputString, boolean return_value) throws IOException {
        int predict_probability = 0;
        try (BufferedReader input = new BufferedReader(new StringReader(inputString));
             DataOutputStream output = new DataOutputStream(
                 new BufferedOutputStream(new ByteArrayOutputStream()))) {
            if (svm.svm_check_probability_model(SVM_Model) != 0) {
                System.out
                    .print("Model supports probability estimates, but disabled in prediction.\n");
            }
            return predict(input, output, SVM_Model, predict_probability, return_value);
        } catch (FileNotFoundException | ArrayIndexOutOfBoundsException e) {
            exit_with_help();
            return 0;
        }
    }
}

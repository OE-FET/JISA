package jisa.maths.matrices;

public interface QR<T> {

    Matrix<T> getQ();

    Matrix<T> getR();

    Matrix<T> getQT();

    Matrix<T> getH();

}

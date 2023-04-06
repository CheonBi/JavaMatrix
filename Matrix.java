package TF.Util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class Matrix {

	private double[][] data = null;
	private int rows = 0, cols = 0;

	public Matrix() {
		
	}

	public Matrix(int rows, int cols) {
		this.data = new double[rows][cols];
		this.rows = rows;
		this.cols = cols;
	}

	public Matrix(int square) {
		this.data = new double[square][square];
		this.rows = square;
		this.cols = square;
	}

	public Matrix(double[][] data) {
		this.data = data.clone();
		this.rows = this.data.length;
		this.cols = this.data[0].length;
	}

	/**
	 * flag parameter가 true이면 열(Col) 크키가 1인 행렬 (Row * 1), flag parameter가 false이면
	 * 행(Row) 크키가 1인 행렬 (1 * Col)
	 * 
	 * @param data
	 * @param flag
	 */
	public Matrix(double[] data, boolean flag) {
		/*
		 * Col Size = 1
		 */

		if (flag == true) {
			this.rows = data.length;
			this.cols = 1;
			this.data = new double[rows][cols];

			for (int i = 0; i < rows; i++) {
				this.data[i][0] = data[i];
			}
		}

		/*
		 * Row Size = 1;
		 */
		else if (flag == false) {
			this.rows = 1;
			this.cols = data.length;
			this.data = new double[rows][cols];

			for (int i = 0; i < cols; i++) {
				this.data[0][i] = data[i];
			}
		}
	}

	/**
	 * flag parameter가 true이면 열(Col) 크키가 1인 행렬 (Row * 1), flag parameter가 false이면
	 * 행(Row) 크키가 1인 행렬 (1 * Col)
	 * 
	 * @param data
	 * @param flag
	 */
	public Matrix(Double[] data, boolean flag) {
		if (flag == true) {
			this.rows = data.length;
			this.cols = 1;
			this.data = new double[rows][cols];

			for (int i = 0; i < rows; i++) {
				this.data[i][0] = data[i].doubleValue();
			}
		}

		/*
		 * Row Size = 1;
		 */
		else if (flag == false) {
			this.rows = 1;
			this.cols = data.length;
			this.data = new double[rows][cols];

			for (int i = 0; i < cols; i++) {
				this.data[0][i] = data[i].doubleValue();
			}
		}
	}
	
	
	/**
	 * flag parameter가 true이면 열(Col) 크키가 1인 행렬 (Row * 1), flag parameter가 false이면
	 * 행(Row) 크키가 1인 행렬 (1 * Col)
	 * 
	 * @param size
	 * @param flag
	 */
	public Matrix(int size, boolean flag) {
		if(flag == true) {
			this.rows = size;
			this.cols = 1;
			this.data = new double[this.rows][1];
		} 
		
		else if(flag == false) {
			this.rows = 1;
			this.cols = size;
			this.data = new double[1][this.cols];
		}
	}
	
	
	/**
	 * flag parameter가 true이면 열(Col) 크키가 1인 행렬 (Row * 1), flag parameter가 false이면
	 * 행(Row) 크키가 1인 행렬 (1 * Col)
	 * 
	 * @param data
	 * @param flag
	 */
	public Matrix(ArrayList<Double> data, boolean flag) {
		if (flag == true) {
			this.rows = data.size();
			this.cols = 1;
			this.data = new double[rows][cols];

			for (int i = 0; i < rows; i++) {
				this.data[i][0] = data.get(i).doubleValue();
			}
		}

		/*
		 * Row Size = 1;
		 */
		else if (flag == false) {
			this.rows = 1;
			this.cols = data.size();
			this.data = new double[rows][cols];

			for (int i = 0; i < cols; i++) {
				this.data[0][i] = data.get(i).doubleValue();
			}
		}
	}

	public boolean isSquare() {
		return rows == cols;
	}

	public void setMatrix(double[][] data) {
		this.data = data.clone();
		rows = this.data.length;
		cols = this.data[0].length;
	}
	

	public void setZero() {
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				this.data[i][j] = 0;
			}
		}
	}

	public void setOnes() {
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				this.data[i][j] = 1;
			}
		}
	}

	public void setElement(int row, int col, double element) {
		if (this.data.length > 0) {
			this.data[row][col] = element;
		}
	}

	public void setRow(int row, double[] rowArray) throws IndexOutOfBoundsException {
		if (this.cols != rowArray.length) {
			return;
		}

		if (this.data.length > 0) {
			for (int i = 0; i < rowArray.length; i++) {
				this.data[row][i] = rowArray[i];
			}
		}
	}

	public void setCol(int col, double[] colArray) throws IndexOutOfBoundsException {
		if (this.rows != colArray.length) {
			return;
		}
		if (this.data.length > 0) {
			for (int i = 0; i < colArray.length; i++) {
				this.data[i][col] = colArray[i];
			}
		}
	}

	/**
	 * @param row
	 * @param col
	 * @return Matrix [row][col] 원소 반환, 행렬의 크기가 0일시 0반환
	 */
	public double getElement(int row, int col) {
		double element = 0;
		if (this.data.length > 0) {
			element = data[row][col];
		}
		return element;
	}

	/**
	 * @param row
	 * @return 
	 */
	public double getElement(int e, boolean flag) {
		double element = 0;
		int row = this.data.length;
		int col = this.data[0].length;
		
		/* Col Size = 1 */
		if(flag == true && (row > 0)) {
			element = data[e][0];
		} 
		
		/* Row Size = 1 */
		else if(flag == false && (col > 0)) {
			element = data[0][e];
		}
		return element;
	}

	/**
	 * @return Matrix 배열 리턴
	 */
	public double[][] getMatrix() {
		return data;
	}
	
	public Matrix clone() {
		return this;
	}

	public static boolean equalsize(Matrix A, Matrix B) {
		if (A.getRowSize() == B.getRowSize() && A.getColSize() == B.getColSize()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return 영행렬
	 */
	public static Matrix zeros(int row, int col) {
		Matrix zero = new Matrix(row, col);
		zero.setZero();
		return zero;
	}

	/**
	 * @return 행 크기
	 */
	public int getRowSize() {
		return this.rows;
	}

	/**
	 * @return 열 크기
	 */
	public int getColSize() {
		return this.cols;
	}

	/**
	 * @return 가장 큰 배열 차원의 길이
	 */
	public int length() {
		int length = 0;
		if (this.rows > this.cols) {
			length = rows;
		}

		else if (this.rows < this.cols) {
			length = cols;
		}

		return length;
	}

	/**
	 * 행렬의 합 Matrix A + Matrix B
	 */
	public Matrix sum(Matrix A, Matrix B) {
		double[][] sum = Matrix.zeros(A.rows, A.cols).getMatrix();
		double[][] tempA = A.getMatrix();
		double[][] tempB = B.getMatrix();

		if (equalsize(A, B)) {
			for (int i = 0; i < tempA.length; i++) {
				for (int j = 0; j < tempA[0].length; j++) {
					sum[i][j] = tempA[i][j] + tempB[i][j];
				}
			}
			return new Matrix(sum);
		}
		return new Matrix(sum);
	}

	/**
	 * 행렬의 차 Matrix A - Matrix B
	 */
	public Matrix diff(Matrix A, Matrix B) {
		double[][] diff = Matrix.zeros(A.rows, A.cols).getMatrix();
		double[][] tempA = A.getMatrix();
		double[][] tempB = B.getMatrix();

		if (equalsize(A, B)) {
			for (int i = 0; i < tempA.length; i++) {
				for (int j = 0; j < tempA[0].length; j++) {
					diff[i][j] = tempA[i][j] - tempB[i][j];
				}
			}
			return new Matrix(diff);
		}
		return new Matrix(diff);
	}

	/**
	 * 행렬의 곱 Matrix A * Matrix B
	 */
	public Matrix multiply(Matrix A, Matrix B) {
		double[][] multiply = Matrix.zeros(A.rows, B.cols).getMatrix();
		double[][] tempA = A.getMatrix();
		double[][] tempB = B.getMatrix();

		for (int row = 0; row < multiply.length; row++) {
			for (int col = 0; col < multiply[0].length; col++) {
				for (int i = 0; i < B.rows; i++) {
					multiply[row][col] = tempA[row][i] * tempB[i][col];
				}
			}
		}
		return new Matrix(multiply);
	}

	/**
	 * @param row
	 * @return 특정 열의 데이터(배열)
	 */
	public double[] getRowArray(int row) {
		double[][] temp = this.data;
		double[] RowArr = new double[temp[0].length];

		for (int i = 0; i < temp[row].length; i++) {
			RowArr[i] = temp[row][i];
		}
		return RowArr;
	}

	/**
	 * @param col
	 * @return 특정 행의 데이터(배열)
	 */
	public double[] getColArray(int col) {
		double[][] temp = this.data;
		double[] ColArr = new double[temp.length];

		for (int i = 0; i < temp.length; i++) {
			ColArr[i] = temp[i][col];
		}
		return ColArr;
	}

	/**
	 * @return double Array(column)
	 */
	public double[] columnSum() {
		double[] sum = new double[this.cols];
		for (int i = 0; i < this.cols; i++) {
			for (int j = 0; j < this.rows; j++) {
				sum[i] += this.data[j][i];
			}
		}
		return sum;
	}

	/**
	 * 
	 * @return double Array(row)
	 */
	public double[] rowSum() {
		double[] sum = new double[this.rows];
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				sum[i] += this.data[i][j];
			}
		}
		return sum;
	}

	public double scalaSum() {
		double sum = 0;

		if (this.rows < 2) {
			for (int i = 0; i < this.cols; i++) {
				for (int j = 0; j < this.rows; j++) {
					sum += this.data[j][i];
				}
			}
		}

		else if (this.cols < 2) {
			for (int i = 0; i < this.rows; i++) {
				for (int j = 0; j < this.cols; j++) {
					sum += this.data[i][j];
				}
			}
		}

		return sum;
	}

	public void printMatrix() {
		for (int i = 0; i < this.data.length; i++) {
			for (int j = 0; j < this.data[0].length; j++) {
				System.out.printf("%.4f ", this.getMatrix()[i][j]);
			}
			System.out.println("");
		}
	}

	public void saveMatrix(File file) {

		try {
			FileOutputStream out = new FileOutputStream(file);
			DataOutputStream dos = new DataOutputStream(out);
			for (int i = 0; i < this.data.length; i++) {
				for (int j = 0; j < this.data[0].length; j++) {
					String formatDouble = String.format("%.4f", this.getMatrix()[i][j]);
					dos.writeBytes(formatDouble);
					dos.writeBytes(",");
				}
				dos.writeBytes("\r\n");
			}
			dos.flush();
			dos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		finally {

		}

	}
}

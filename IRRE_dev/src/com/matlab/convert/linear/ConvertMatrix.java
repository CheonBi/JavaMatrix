package com.matlab.convert.linear;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

import com.matlab.convert.mat.Matrix;

public class ConvertMatrix {

	private File updonwFile = new File("data/csv/up_down_mid_output.csv");
	private File minmaxFile = new File("data/csv/max_min_critical_output.csv");
	private File scheduleFile = new File("data/csv/schedule_output.csv");

	Matrix up_down_mid = new Matrix(288, 3);
	Matrix min_max_critical = new Matrix(184, 3);
	Matrix data_schedule = new Matrix(184, 288);

	Matrix upScore = null;
	Matrix downScore = null;
	Matrix midScore = null;

	Matrix max = null;
	Matrix min = null;
	Matrix critical = null;

	double period = 5;
	Matrix start_up_time = null;

	public ConvertMatrix() {
		BufferedReader up_down = null;
		BufferedReader min_max = null;
		BufferedReader schedule = null;

		try {
			up_down = new BufferedReader(new FileReader(updonwFile));
			min_max = new BufferedReader(new FileReader(minmaxFile));
			schedule = new BufferedReader(new FileReader(scheduleFile));

			String line_updown = up_down.readLine();
			String line_minmax = min_max.readLine();
			String line_sch = schedule.readLine();

			double[] sch = new double[data_schedule.getRowSize()];

			int row1 = 0, row2 = 0, row3 = 0;

			while ((line_updown = up_down.readLine()) != null) {
				String[] token = line_updown.split(",");
				up_down_mid.setElement(row1, 0, Double.parseDouble(token[1]));
				up_down_mid.setElement(row1, 1, Double.parseDouble(token[2]));
				up_down_mid.setElement(row1, 2, Double.parseDouble(token[3]));
				row1++;
			}

			while ((line_minmax = min_max.readLine()) != null) {
				String[] token = line_minmax.split(",");
				min_max_critical.setElement(row2, 0, Double.parseDouble(token[1]));
				min_max_critical.setElement(row2, 1, Double.parseDouble(token[2]));
				min_max_critical.setElement(row2, 2, Double.parseDouble(token[3]));
				row2++;
			}
			
			while ((line_sch = schedule.readLine()) != null) {
				String [] token = line_sch.split(",");
				for (int i = 0; i< token.length -1; i++) {
					data_schedule.setElement(row3, i, Double.parseDouble(token[i+1]));
				}
				row3++;
			}

			for (int i = 0; i < data_schedule.getRowSize(); i++) {
				sch[i] = Math.round(Math.random() * 2);
			}

			upScore = new Matrix(up_down_mid.getColArray(0), true);
			downScore = new Matrix(up_down_mid.getColArray(1), true);
			midScore = new Matrix(up_down_mid.getColArray(2), true);
		

			max = new Matrix(min_max_critical.getColArray(0), true);
			min = new Matrix(min_max_critical.getColArray(1), true);
			critical = new Matrix(min_max_critical.getColArray(2), true);
			
			start_up_time = new Matrix(sch, false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		ConvertMatrix convert = new ConvertMatrix();
		System.out.println("Convert");

		Matrix scoreLoad = new Matrix(288, 1).diff(convert.upScore, convert.midScore);
		scoreLoad = scoreLoad.diff(scoreLoad, convert.downScore);

		Matrix scoreLoad_step = new Matrix(288, 1);

		for (int i = 0; i < scoreLoad.getMatrix().length; i++) {
			if (i == scoreLoad.getMatrix().length - 1) {
				scoreLoad_step.setElement(i, 0, scoreLoad.getElement(0, 0));
			}

			else {
				scoreLoad_step.setElement(i, 0, scoreLoad.getElement(i + 1, 0));
			}
		}

		Matrix load_change = new Matrix(288, 1).diff(scoreLoad_step, scoreLoad);
		Matrix load_change_positive = new Matrix(load_change.getColSize(), load_change.getRowSize());
		Matrix load_change_negative = new Matrix(load_change.getColSize(), load_change.getRowSize());
	

		for (int i = 0; i < load_change.length(); i++) {
			if (load_change.getElement(i) > 0) {
				load_change_positive.setElement(0, i, load_change.getElement(i));
			} else {
				load_change_positive.setElement(0, i, 0);
			}
		}


		for (int i = 0; i < load_change.length(); i++) {
			if (load_change.getElement(i) < 0) {
				load_change_negative.setElement(0, i, Math.abs(load_change.getElement(i)));
			}

			else {
				load_change_negative.setElement(0, i, 0);
			}
		}

		int on_off;
		int row = convert.data_schedule.getRowSize();
		int col = convert.data_schedule.getColSize();
		
		Matrix flx_up = new Matrix(row, col);
		Matrix up_ward_flx = new Matrix(col, row);
		Matrix pro_up = new Matrix(col, 2000);
		Matrix prob_up = new Matrix(2000, row);
		Matrix prob_cdf_upward = new Matrix(2000, row);
		double time_period;

		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {

				if (convert.data_schedule.getElement(i, j) == 0) {
					on_off = 0;
				}

				else {
					on_off = 1;
				}

				/* ------------------------------------------ */

				time_period = (convert.period - (1 - on_off) * convert.start_up_time.getElement(0, i));

				if (time_period < 0) { 
					flx_up.setElement(i, j, 0);
				}

				else {
					double element = convert.critical.getElement(i, 0) * time_period;
					flx_up.setElement(i, j, element);
				}
				
				

				/* ------------------------------------------ */

				if ( (convert.data_schedule.getElement(i, j) + flx_up.getElement(i, j)) >= convert.max.getElement(i, 0))

				{
					double element1 = convert.max.getElement(i, 0);
					double element2 = convert.data_schedule.getElement(i, j);
					up_ward_flx.setElement(j, i, element1 - element2);
				}

				else {
					double element = flx_up.getElement(i, j);
					up_ward_flx.setElement(j, i, element);
				}
			}
		}
	

		pro_up.setZero();

		for (int k = 0; k < row; k++) { 
			for (int i = 0; i < 2000; i++) {
				for (int j = 0; j < col; j++) {
					if (up_ward_flx.getElement(j, k) < i) {
						double element = pro_up.getElement(j, i) + 1;
						pro_up.setElement(j, i, element);
					} 
					
					else {
						
					}
				}
			}

			double[] pro_tem = pro_up.columnSum();
			prob_up.setCol(k, pro_tem);
			pro_up.setZero();
		}

		for (int i = 0; i < row; i++) {
			double[] upward = prob_up.getColArray(i);

			for (int j = 0; j < upward.length; j++) {
				upward[j] = upward[j] / col;
			}
			prob_cdf_upward.setCol(i, upward);
		}
		
		Matrix flx_down = new Matrix(row, col);
		Matrix down_ward_flx = new Matrix(col, row);
		Matrix pro_down = new Matrix(col, 2000);
		Matrix prob_down = new Matrix(2000, row);
		Matrix prob_cdf_downward = new Matrix(2000, row);

		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {

				if (convert.data_schedule.getElement(i, j) == 0) {
					on_off = 0;
				}

				else {
					on_off = 1;
				}

				double flx_down_element = convert.critical.getElement(i, 0) * convert.period * on_off;

				flx_down.setElement(i, j, flx_down_element);

				/* ------------------------------------------ */

				if (convert.data_schedule.getElement(i, j) - flx_down.getElement(i, j) <= convert.min.getElement(i,0))
				{
					double element1 = convert.min.getElement(i, 0);
					double element2 = convert.data_schedule.getElement(i, j);
					down_ward_flx.setElement(j, i, element2 - element1);
				}

				else {
					double element = flx_down.getElement(i, j);
					down_ward_flx.setElement(j, i, element);
				}
			}
		}

		pro_down.setZero();

		for (int k = 0; k < row; k++) {
			for (int i = 0; i < 2000; i++) {
				for (int j = 0; j < col; j++) {
					if (down_ward_flx.getElement(j, k) < i) {
						double element = pro_down.getElement(j, i) + 1;
						pro_down.setElement(j, i, element);
					}
				}
			}

			double[] pro_tem = pro_down.columnSum(); 
			prob_down.setCol(k, pro_tem);
			pro_down.setZero();
		}

		for (int i = 0; i < row; i++) {
			double[] downward = prob_down.getColArray(i);

			for (int j = 0; j < downward.length; j++) {
				downward[j] = downward[j] / col;
			}
			prob_cdf_downward.setCol(i, downward);
		}

		Matrix need_up = load_change_positive;
		Matrix need_down = load_change_negative;

		Matrix need_pro_up = new Matrix(1, 184);
		Matrix need_pro_total_up = new Matrix(288, 1);

		Matrix need_pro_down = new Matrix(1, 184);
		Matrix need_pro_total_down = new Matrix(288, 1);
		
		for (int j = 0; j < col; j++) {
			if (need_up.getElement(0, j) > 0) {

				double e;
				double total = 0;

				for (int i = 0; i < row; i++) {
					e = prob_cdf_upward.getElement((int) Math.round(need_up.getElement(0, j)), i);

					need_pro_up.setElement(0, i, e);
					
					total = need_pro_up.scalaSum()/row;
					need_pro_total_up.setElement(j, 0, total);
				}

			}

			else {
				need_pro_total_up.setElement(j, 0, 0);
			}

		}

		for (int j = 0; j < col; j++) {
			if (need_down.getElement(0, j) > 0) {

				double e;
				double total = 0;

				for (int i = 0; i < row; i++) {
					e = prob_cdf_downward.getElement((int) Math.round(need_down.getElement(0, j)), i);

					need_pro_down.setElement(0, i, e);
					
					total = need_pro_down.scalaSum()/ row;
					need_pro_total_down.setElement(j, 0, total);
				}

			}

			else {
				need_pro_total_down.setElement(j, 0, 0);
			}
		}
		
		Matrix need_pro_total = new Matrix().sum(need_pro_total_up, need_pro_total_down);
		double Upward = need_pro_total_up.scalaSum();
		double Downward = need_pro_total_down.scalaSum();
		
		System.out.printf("%.4f ", Upward);
		System.out.println();
		System.out.printf("%.4f ", Downward);
	}
}

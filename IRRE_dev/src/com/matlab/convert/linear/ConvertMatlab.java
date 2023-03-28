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

public class ConvertMatlab {

	private File pvFile = new File("data/csv/up_down_mid_output.csv");
	private File minmaxFile = new File("data/csv/max_min_critical_output.csv");
	private File scheduleFile = new File("data/csv/schedule_output.csv");

	Matrix load_pv_wind = new Matrix(288, 3);
	Matrix min_max_ramp = new Matrix(184, 3);
	Matrix gen_schedule = new Matrix(184, 288);

	Matrix peak_load = null;
	Matrix PV = null;
	Matrix Wind = null;

	Matrix gen_max = null;
	Matrix gen_min = null;
	Matrix gen_ramp = null;

	double period = 5;
	Matrix start_up_time = null;

	public ConvertMatlab() {
		BufferedReader pv = null;
		BufferedReader min_max = null;
		BufferedReader schedule = null;

		try {
			pv = new BufferedReader(new FileReader(pvFile));
			min_max = new BufferedReader(new FileReader(minmaxFile));
			schedule = new BufferedReader(new FileReader(scheduleFile));

			String line_pv = pv.readLine();
			String line_minmax = min_max.readLine();
			String line_sch = schedule.readLine();

			double[] sch = new double[gen_schedule.getRowSize()];

			int row1 = 0, row2 = 0, row3 = 0;

			while ((line_pv = pv.readLine()) != null) {
				String[] token = line_pv.split(",");
				load_pv_wind.setElement(row1, 0, Double.parseDouble(token[1]));
				load_pv_wind.setElement(row1, 1, Double.parseDouble(token[2]));
				load_pv_wind.setElement(row1, 2, Double.parseDouble(token[3]));
				row1++;
			}

			while ((line_minmax = min_max.readLine()) != null) {
				String[] token = line_minmax.split(",");
				min_max_ramp.setElement(row2, 0, Double.parseDouble(token[1]));
				min_max_ramp.setElement(row2, 1, Double.parseDouble(token[2]));
				min_max_ramp.setElement(row2, 2, Double.parseDouble(token[3]));
				row2++;
			}
			
			while ((line_sch = schedule.readLine()) != null) {
				String [] token = line_sch.split(",");
				for (int i = 0; i< token.length -1; i++) {
					gen_schedule.setElement(row3, i, Double.parseDouble(token[i+1]));
				}
				row3++;
			}

			for (int i = 0; i < gen_schedule.getRowSize(); i++) {
				sch[i] = Math.round(Math.random() * 2);
			}

			peak_load = new Matrix(load_pv_wind.getColArray(0), true);
			PV = new Matrix(load_pv_wind.getColArray(1), true);
			Wind = new Matrix(load_pv_wind.getColArray(2), true);
		

			gen_max = new Matrix(min_max_ramp.getColArray(0), true);
			gen_min = new Matrix(min_max_ramp.getColArray(1), true);
			gen_ramp = new Matrix(min_max_ramp.getColArray(2), true);
			
			start_up_time = new Matrix(sch, false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {
		ConvertMatlab convert = new ConvertMatlab();
		System.out.println("Convert");

		/*
		 * Net load ?? ???? Step?? ????? u?
		 */
		Matrix net_load = new Matrix(288, 1).diff(convert.peak_load, convert.Wind);
		net_load = net_load.diff(net_load, convert.PV);

		Matrix net_load_step = new Matrix(288, 1);

		for (int i = 0; i < net_load.getMatrix().length; i++) {
			if (i == net_load.getMatrix().length - 1) {
				net_load_step.setElement(i, 0, net_load.getElement(0, 0));
			}

			else {
				net_load_step.setElement(i, 0, net_load.getElement(i + 1, 0));
			}
		}

		Matrix net_change = new Matrix(288, 1).diff(net_load_step, net_load);
		// net_change.printMatrix();

		/*
		 * Upward/Downward net load ???? : net load step?? ????? + ?? - ????
		 * 
		 */
		Matrix net_change_positive = new Matrix(net_change.getColSize(), net_change.getRowSize());
		Matrix net_change_negative = new Matrix(net_change.getColSize(), net_change.getRowSize());

		/*
		 * Step ????? -> (+)
		 */

		for (int i = 0; i < net_change.length(); i++) {
			if (net_change.getElement(i) > 0) {
				net_change_positive.setElement(0, i, net_change.getElement(i));
			} else {
				net_change_positive.setElement(0, i, 0);
			}
		}

		/*
		 * Step ????? -> (-)
		 */

		for (int i = 0; i < net_change.length(); i++) {
			if (net_change.getElement(i) < 0) {
				net_change_negative.setElement(0, i, Math.abs(net_change.getElement(i)));
			}

			else {
				net_change_negative.setElement(0, i, 0);
			}
		}

		/*
		 * Upward Available Flexible Distribution
		 */
		int on_off;
		int row = convert.gen_schedule.getRowSize();
		int col = convert.gen_schedule.getColSize();
		
		Matrix flx_up = new Matrix(row, col);
		Matrix up_ward_flx = new Matrix(col, row);
		Matrix pro_up = new Matrix(col, 2000);
		Matrix prob_up = new Matrix(2000, row);
		Matrix prob_cdf_upward = new Matrix(2000, row);
		double time_period;

		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {

				if (convert.gen_schedule.getElement(i, j) == 0) {
					on_off = 0;
				}

				else {
					on_off = 1;
				}

				/* ------------------------------------------ */

				time_period = (convert.period - (1 - on_off) * convert.start_up_time.getElement(0, i));

				if (time_period < 0) { // ?????? ?? ?梨�? ????? ?????? ?? ??? ?????? ????
					flx_up.setElement(i, j, 0);
				}

				else {
					double element = convert.gen_ramp.getElement(i, 0) * time_period;
					flx_up.setElement(i, j, element);
				}
				
				

				/* ------------------------------------------ */

				if ( (convert.gen_schedule.getElement(i, j) + flx_up.getElement(i, j)) >= convert.gen_max.getElement(i, 0))

				{
					double element1 = convert.gen_max.getElement(i, 0);
					double element2 = convert.gen_schedule.getElement(i, j);
					up_ward_flx.setElement(j, i, element1 - element2);
				}

				else {
					double element = flx_up.getElement(i, j);
					up_ward_flx.setElement(j, i, element);
				}
			}
		}
	

		pro_up.setZero();

		for (int k = 0; k < row; k++) { /* 2000MW???? ????? ?? ??? ?? ?????? ?? Upward AFD ???? ??? */
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
		
		

		/*
		 * Downward Available Flexible Distribution
		 */
		Matrix flx_down = new Matrix(row, col);
		Matrix down_ward_flx = new Matrix(col, row);
		Matrix pro_down = new Matrix(col, 2000);
		Matrix prob_down = new Matrix(2000, row);
		Matrix prob_cdf_downward = new Matrix(2000, row);

		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {

				if (convert.gen_schedule.getElement(i, j) == 0) {
					on_off = 0;
				}

				else {
					on_off = 1;
				}

				double flx_down_element = convert.gen_ramp.getElement(i, 0) * convert.period * on_off;

				flx_down.setElement(i, j, flx_down_element);

				/* ------------------------------------------ */

				if (convert.gen_schedule.getElement(i, j) - flx_down.getElement(i, j) <= convert.gen_min.getElement(i,0))
				{
					double element1 = convert.gen_min.getElement(i, 0);
					double element2 = convert.gen_schedule.getElement(i, j);
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

			double[] pro_tem = pro_down.columnSum(); // k??吏� ?????? downward IRRP ????
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

		/*
		 * ??? ???????? ???? IRRE ????
		 */

		Matrix need_MW_up = net_change_positive;
		Matrix need_MW_down = net_change_negative;

		Matrix MW_pro_up = new Matrix(1, 184);
		Matrix MW_pro_total_up = new Matrix(288, 1);

		Matrix MW_pro_down = new Matrix(1, 184);
		Matrix MW_pro_total_down = new Matrix(288, 1);
		
		for (int j = 0; j < col; j++) {
			if (need_MW_up.getElement(0, j) > 0) {

				double e;
				double total = 0;

				for (int i = 0; i < row; i++) {
					e = prob_cdf_upward.getElement((int) Math.round(need_MW_up.getElement(0, j)), i);
					

					MW_pro_up.setElement(0, i, e);
					
					total = MW_pro_up.scalaSum()/row;
					MW_pro_total_up.setElement(j, 0, total);
				}

			}

			else {
				MW_pro_total_up.setElement(j, 0, 0);
			}

		}

		for (int j = 0; j < col; j++) {
			if (need_MW_down.getElement(0, j) > 0) {

				double e;
				double total = 0;

				for (int i = 0; i < row; i++) {
					e = prob_cdf_downward.getElement((int) Math.round(need_MW_down.getElement(0, j)), i);
					

					MW_pro_down.setElement(0, i, e);
					
					total = MW_pro_down.scalaSum()/ row;
					MW_pro_total_down.setElement(j, 0, total);
				}

			}

			else {
				MW_pro_total_down.setElement(j, 0, 0);
			}
		}
		
		Matrix MW_pro_total = new Matrix().sum(MW_pro_total_up, MW_pro_total_down);
		double Upward_IRRE = MW_pro_total_up.scalaSum();
		double Downward_IRRE = MW_pro_total_down.scalaSum();
		
		System.out.printf("%.4f ", Upward_IRRE);
		System.out.println();
		System.out.printf("%.4f ", Downward_IRRE);
	}
}

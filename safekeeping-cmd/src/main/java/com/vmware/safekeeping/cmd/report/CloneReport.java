/**
 *
 */
package com.vmware.safekeeping.cmd.report;

import org.apache.commons.lang.StringUtils;

import com.vmware.safekeeping.core.command.report.RunningReport;
import com.vmware.safekeeping.core.control.IoFunction;

public class CloneReport extends RunningReport {

	public CloneReport() {
	}

	@Override
	public void run() {
		initializeReport();
		IoFunction.println();
		IoFunction.print("0%|");
		IoFunction.print(StringUtils.repeat("---------", "|", 10));
		IoFunction.println("|100%");
		IoFunction.print("   ");
		try {
			int previousProgressPercent = 0;
			int isTen = 0;
			while (this.progressPercent < 100) {

				for (int i = 0; i < (this.progressPercent - previousProgressPercent); i++) {
					if (++isTen > 9) {
						IoFunction.print('O');
						isTen = 0;
					} else {
						IoFunction.print('o');
					}
				}
				previousProgressPercent = this.progressPercent;
				Thread.sleep(2000);

			}
		} catch (final InterruptedException e) {

		}
		if (this.error) {
			IoFunction.println();
			IoFunction.println("Error: " + this.errorMessage);
		} else {
			if (this.progressPercent != 100) {
				for (int i = this.progressPercent; i < 99; i++) {
					IoFunction.print('o');
				}
				IoFunction.print('O');
			}
		}
		IoFunction.println();

	}
}
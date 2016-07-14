package nl.weeaboo.vn.save.impl;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import nl.weeaboo.common.Checks;
import nl.weeaboo.vn.core.IProgressListener;

public final class ProgressOutputStream extends FilterOutputStream {

	private final int updateBytes;
	private final IProgressListener pl;

	private int lastReportedPos;
	private int pos;

    private ProgressOutputStream(OutputStream out, int updateBytes, IProgressListener pl) {
		super(out);

        this.updateBytes = Checks.checkRange(updateBytes, "updateBytes", 1);
        this.pl = Checks.checkNotNull(pl);
	}

    public static OutputStream wrap(OutputStream out, IProgressListener pl) {
        return wrap(out, 2048, pl);
    }

    /**
     * @param pl May be null.
     */
    public static OutputStream wrap(OutputStream out, int updateBytes, IProgressListener pl) {
        if (pl != null) {
            return new ProgressOutputStream(out, updateBytes, pl);
        } else {
            return out;
        }
    }

	@Override
    public void write(byte b[], int off, int len) throws IOException {
		out.write(b, off, len);

		pos += len;
   		if (pos - lastReportedPos >= updateBytes) {
            lastReportedPos = (pos / updateBytes) * updateBytes;
   			pl.onProgressChanged(lastReportedPos);
   		}
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);

   		pos++;
   		if (pos - lastReportedPos >= updateBytes) {
            lastReportedPos = (pos / updateBytes) * updateBytes;
   			pl.onProgressChanged(lastReportedPos);
   		}
	}

}

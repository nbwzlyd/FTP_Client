package ftpcom.lyd.ftpandroidclient.chain;

import android.widget.EditText;

public class PortChain extends LoginChain {
    public PortChain(EditText editText) {
        super(editText);
    }

    @Override
    protected String errorStr() {
        return "端口为空";
    }

    @Override
    protected String OKStr() {
        return "ok";
    }
}

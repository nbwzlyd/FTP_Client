package ftpcom.lyd.ftpandroidclient.chain;

import android.widget.EditText;

public class IpChain extends LoginChain {
   public IpChain(EditText editText) {
        super(editText);
    }

    @Override
    protected String errorStr() {
        return "ip不能为空";
    }

    @Override
    public String OKStr() {
        return "OK";
    }
}

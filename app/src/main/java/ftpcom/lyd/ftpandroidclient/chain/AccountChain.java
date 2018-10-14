package  ftpcom.lyd.ftpandroidclient.chain;

import android.widget.EditText;

public class AccountChain extends LoginChain {
    public AccountChain(EditText editText) {
        super(editText);
    }

    @Override
    protected String errorStr() {
        return "账户不能为空";
    }

    @Override
    protected String OKStr() {
        return super.OKStr();
    }


}

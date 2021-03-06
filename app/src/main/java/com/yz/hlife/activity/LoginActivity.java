package com.yz.hlife.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.inter.YzStatusListener;
import com.http.network.model.RequestWork;
import com.http.network.model.ResponseWork;
import com.jaeger.library.StatusBarUtil;
import com.work.api.open.model.LoginResp;
import com.work.api.open.model.SysUserReq;
import com.work.api.open.model.client.OpenData;
import com.work.util.AppUtils;
import com.work.util.KeyboardUtils;
import com.work.util.SLog;
import com.work.util.SharedUtils;
import com.work.util.ToastUtil;
import com.workstation.permission.PermissionsResultAction;
import com.yz.hlife.R;

/**
 * Created by tangyx
 * Date 2020/8/17
 * email tangyx@live.com
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener, TextWatcher {
    public static String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private EditText mPhone;
    private EditText mPassword;
    private EditText mMobile;
    private Button mSubmit;

    @Override
    public void onInitView() throws Exception {
        super.onInitView();
        mPhone = findViewById(R.id.phone);
        mPassword = findViewById(R.id.password);
        mMobile = findViewById(R.id.mobile);
        mSubmit = findViewById(R.id.submit);
        TextView mVersion = findViewById(R.id.version);
        AppUtils.AppInfo appInfo = AppUtils.getAppInfo(this);
        if(appInfo!=null){
            mVersion.setText(getString(R.string.text_version,appInfo.getVersionName()));
        }
        findViewById(R.id.forget).setOnClickListener(this);
        findViewById(R.id.register).setOnClickListener(this);
        findViewById(R.id.submit).setOnClickListener(this);
//        if(!TextUtils.isEmpty(UserApi.instance().getToken())){
//            startActivity(new Intent(LoginActivity.this,DataTestActivity.class));
//            finish();
//        }
    }

    @Override
    public void onInitValue() throws Exception {
        super.onInitValue();
        setSubEnable(false);
        mPhone.addTextChangedListener(this);
        mPassword.addTextChangedListener(this);
        if(!hasPermission(PERMISSIONS)){
            onPermissionChecker(PERMISSIONS, new PermissionsResultAction() {
                @Override
                public void onGranted() {
                }

                @Override
                public void onDenied(String permission) {
                    ToastUtil.error(LoginActivity.this,R.string.toast_permission_error);
                }
            });
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        StatusBarUtil.setLightMode(this);
    }

    @Override
    public void setStatusBar() {
    }

    private void setSubEnable(boolean enable){
        if(enable){
            mSubmit.setEnabled(true);
        }else{
            mSubmit.setEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPhone.setText(SharedUtils.getString("userid"));
        mPhone.setText("1850000000054892407893891");
        mPassword.setText(SharedUtils.getString("nickname"));
        mPassword.setText("test2test2");
        mMobile.setText(SharedUtils.getString("mobile"));
        mMobile.setText("18500000001");
    }

    @Override
    public boolean isShowTitleBar() {
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.forget:
                startActivity(new Intent(this, RegisterActivity.class).putExtra(RegisterActivity.class.getSimpleName(),false));
                break;
            case R.id.register:
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.submit:
//                LoginReq loginReq = new LoginReq();
//                loginReq.setMobile(mPhone.getText().toString().trim());
//                loginReq.setPassword(mPassword.getText().toString().trim());
//                mSubmit.setEnabled(false);
//                showProgressLoading(false,false);
//                Yz.getSession().login(loginReq,this);
                String userid = mPhone.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String mobile = mMobile.getText().toString().trim();
                if(TextUtils.isEmpty(userid) || TextUtils.isEmpty(password) || TextUtils.isEmpty(mobile)){
                    return;
                }

                SysUserReq sysUserReq = new SysUserReq();
                sysUserReq.setUserId(userid);
                sysUserReq.setMobile(mobile);
                sysUserReq.setNickName(password);
                YzIMKitAgent.instance().register(sysUserReq, new YzStatusListener() {
                    @Override
                    public void loginSuccess(Object data) {
                        super.loginSuccess(data);
                        SharedUtils.putData("userid",userid);
                        SharedUtils.putData("mobile",mobile);
                        SharedUtils.putData("nickname",password);
                        startActivity(new Intent(LoginActivity.this,DataTestActivity.class));
//                        YzIMKitAgent.instance().startAuto();
                        finish();
                    }

                    @Override
                    public void loginFail(String module, int errCode, String errMsg) {
                        super.loginFail(module, errCode, errMsg);
                        SLog.e(errCode+">>"+errMsg);
                        ToastUtil.error(LoginActivity.this,errCode+">"+errMsg);
                    }
                });
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        setSubEnable(!TextUtils.isEmpty(mPhone.getText().toString().trim()) && !TextUtils.isEmpty(mPassword.getText().toString().trim()));
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onResult(RequestWork req, ResponseWork resp) throws Exception {
        super.onResult(req, resp);
        mSubmit.setEnabled(true);
        if(resp.isSuccess()){
            if(resp instanceof LoginResp){
                OpenData data = ((LoginResp) resp).getData();
                if(data!=null){
                    showProgressLoading(false,false);
                    SysUserReq sysUserReq = new SysUserReq();
                    sysUserReq.setUserId(data.getUserId());
                    sysUserReq.setNickName(data.getNickName());
                    sysUserReq.setUserIcon(data.getUserIcon());
                    sysUserReq.setMobile(mPhone.getText().toString().trim());
                    sysUserReq.setDepartmentId(data.getDepartmentId());
                    sysUserReq.setDepartName(data.getDepartName());
                    sysUserReq.setPosition(data.getPosition());
                    sysUserReq.setCard(data.getCard());
                    sysUserReq.setEmail(data.getEmail());
                    sysUserReq.setCity(data.getCity());
                    sysUserReq.setUserSignature(data.getUserSignature());
                    sysUserReq.setGender(data.getGender());
                    YzIMKitAgent.instance().register(sysUserReq, new YzStatusListener() {
                        @Override
                        public void loginSuccess(Object data) {
                            super.loginSuccess(data);
                            SLog.e("im success:"+data);
                            dismissProgress();
                            finish();
                            YzIMKitAgent.instance().startAuto();
//                            startActivity(new Intent(LoginActivity.this,DataTestActivity.class));
                        }

                        @Override
                        public void loginFail(String module, int errCode, String errMsg) {
                            super.loginFail(module, errCode, errMsg);
                            dismissProgress();
                            ToastUtil.error(LoginActivity.this,"????????????, errCode = " + errCode + ",errInfo = " + errMsg);
                            SLog.e("????????????, errCode = " + errCode + ",errInfo = " + errMsg);
                        }
                    });
                }
            }
        }else{
            KeyboardUtils.hideSoftInput(this);
            ToastUtil.warning(this,resp.getMessage());
        }
    }
}

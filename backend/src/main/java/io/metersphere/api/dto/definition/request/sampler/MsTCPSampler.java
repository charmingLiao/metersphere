package io.metersphere.api.dto.definition.request.sampler;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import io.metersphere.api.dto.definition.request.MsTestElement;
import io.metersphere.api.dto.definition.request.ParameterConfig;
import io.metersphere.api.dto.definition.request.processors.pre.MsJSR223PreProcessor;
import io.metersphere.api.dto.scenario.KeyValue;
import io.metersphere.api.dto.scenario.environment.EnvironmentConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.modifiers.UserParameters;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = "TCPSampler")
public class MsTCPSampler extends MsTestElement {
    @JSONField(ordinal = 20)
    private String type = "TCPSampler";
    @JSONField(ordinal = 21)
    private String classname = "";
    @JSONField(ordinal = 22)
    private String server = "";
    @JSONField(ordinal = 23)
    private String port = "";
    @JSONField(ordinal = 24)
    private String ctimeout = "";
    @JSONField(ordinal = 25)
    private String timeout = "";
    @JSONField(ordinal = 26)
    private boolean reUseConnection = true;
    @JSONField(ordinal = 27)
    private boolean nodelay;
    @JSONField(ordinal = 28)
    private boolean closeConnection;
    @JSONField(ordinal = 29)
    private String soLinger = "";
    @JSONField(ordinal = 30)
    private String eolByte = "";
    @JSONField(ordinal = 31)
    private String username = "";
    @JSONField(ordinal = 32)
    private String password = "";
    @JSONField(ordinal = 33)
    private String request;
    @JSONField(ordinal = 34)
    private Object requestResult;
    @JSONField(ordinal = 35)
    private List<KeyValue> parameters;
    @JSONField(ordinal = 36)
    private String useEnvironment;
    @JSONField(ordinal = 37)
    private MsJSR223PreProcessor tcpPreProcessor;

    @Override
    public void toHashTree(HashTree tree, List<MsTestElement> hashTree, ParameterConfig config) {
        if (!this.isEnable()) {
            return;
        }
        if (this.getReferenced() != null && this.getReferenced().equals("REF")) {
            this.getRefElement(this);
        }
        config.setConfig(getEnvironmentConfig(useEnvironment));
        parseEnvironment(config.getConfig());
        final HashTree samplerHashTree = new ListedHashTree();
        samplerHashTree.add(tcpConfig());
        tree.set(tcpSampler(config), samplerHashTree);
        setUserParameters(samplerHashTree);
        samplerHashTree.add(tcpPreProcessor.getJSR223PreProcessor());
        if (CollectionUtils.isNotEmpty(hashTree)) {
            hashTree.forEach(el -> {
                el.toHashTree(samplerHashTree, el.getHashTree(), config);
            });
        }
    }

    private void parseEnvironment(EnvironmentConfig config) {
        if (config != null && config.getTcpConfig() != null) {
            this.server = config.getTcpConfig().getServer();
            this.port = config.getTcpConfig().getPort();
        }
    }

    private TCPSampler tcpSampler(ParameterConfig config) {
        TCPSampler tcpSampler = new TCPSampler();
        tcpSampler.setName(this.getName());
        if (config != null && StringUtils.isNotEmpty(config.getStep())) {
            tcpSampler.setName(this.getName() + "<->" + config.getStep());
        }

        tcpSampler.setProperty(TestElement.TEST_CLASS, TCPSampler.class.getName());
        tcpSampler.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TCPSamplerGui"));
        tcpSampler.setClassname(this.getClassname());
        tcpSampler.setServer(this.getServer());
        tcpSampler.setPort(this.getPort());
        tcpSampler.setConnectTimeout(this.getCtimeout());
        tcpSampler.setProperty(TCPSampler.RE_USE_CONNECTION, this.isReUseConnection());
        tcpSampler.setProperty(TCPSampler.NODELAY, this.isNodelay());
        tcpSampler.setCloseConnection(String.valueOf(this.isCloseConnection()));
        tcpSampler.setSoLinger(this.getSoLinger());
        tcpSampler.setEolByte(this.getEolByte());
        tcpSampler.setRequestData(this.getRequest());
        tcpSampler.setProperty(ConfigTestElement.USERNAME, this.getUsername());
        tcpSampler.setProperty(ConfigTestElement.PASSWORD, this.getPassword());
        return tcpSampler;
    }

    private void setUserParameters(HashTree tree) {
        UserParameters userParameters = new UserParameters();
        userParameters.setEnabled(true);
        userParameters.setName(this.getName() + "UserParameters");
        userParameters.setPerIteration(false);
        userParameters.setProperty(TestElement.TEST_CLASS, UserParameters.class.getName());
        userParameters.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("UserParametersGui"));
        List<StringProperty> names = new ArrayList<>();
        List<StringProperty> threadValues = new ArrayList<>();
        this.parameters.forEach(item -> {
            names.add(new StringProperty(new Integer(new Random().nextInt(1000000)).toString(), item.getName()));
            threadValues.add(new StringProperty(new Integer(new Random().nextInt(1000000)).toString(), item.getValue()));
        });
        userParameters.setNames(new CollectionProperty(UserParameters.NAMES, names));
        List<CollectionProperty> collectionPropertyList = new ArrayList<>();
        collectionPropertyList.add(new CollectionProperty(new Integer(new Random().nextInt(1000000)).toString(), threadValues));
        userParameters.setThreadLists(new CollectionProperty(UserParameters.THREAD_VALUES, collectionPropertyList));
        tree.add(userParameters);
    }

    private ConfigTestElement tcpConfig() {
        ConfigTestElement configTestElement = new ConfigTestElement();
        configTestElement.setEnabled(true);
        configTestElement.setName(this.getName());
        configTestElement.setProperty(TestElement.TEST_CLASS, ConfigTestElement.class.getName());
        configTestElement.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("TCPConfigGui"));
        configTestElement.setProperty(TCPSampler.CLASSNAME, this.getClassname());
        configTestElement.setProperty(TCPSampler.SERVER, this.getServer());
        configTestElement.setProperty(TCPSampler.PORT, this.getPort());
        configTestElement.setProperty(TCPSampler.TIMEOUT_CONNECT, this.getCtimeout());
        configTestElement.setProperty(TCPSampler.RE_USE_CONNECTION, this.isReUseConnection());
        configTestElement.setProperty(TCPSampler.NODELAY, this.isNodelay());
        configTestElement.setProperty(TCPSampler.CLOSE_CONNECTION, this.isCloseConnection());
        configTestElement.setProperty(TCPSampler.SO_LINGER, this.getSoLinger());
        configTestElement.setProperty(TCPSampler.EOL_BYTE, this.getEolByte());
        configTestElement.setProperty(TCPSampler.SO_LINGER, this.getSoLinger());
        configTestElement.setProperty(ConfigTestElement.USERNAME, this.getUsername());
        configTestElement.setProperty(ConfigTestElement.PASSWORD, this.getPassword());
        return configTestElement;
    }

}

package com.benym.rpas.architecture.template;

import com.benym.rpas.architecture.consts.TemplateType;
import com.benym.rpas.architecture.pojo.FileVO;
import org.springframework.stereotype.Component;

/**
 * starter项目
 *
 * @author benym
 * @date 2022/7/21 10:37 上午
 */
@Component
public class StarterTemplate extends AbstractBuildTemplate {

    @Override
    protected String getTemplateType() {
        return TemplateType.STARTER.getCode();
    }

    @Override
    protected void initPath() {

    }

    @Override
    protected void resolve() {

    }

    @Override
    protected FileVO create() {
        return null;
    }
}

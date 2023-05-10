package cn.rpamis.architecture.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import cn.rpamis.architecture.config.BaseProjectConfig;
import cn.rpamis.architecture.consts.ProjectPath;
import cn.rpamis.architecture.pojo.FileVO;
import cn.rpamis.architecture.service.BuildService;
import cn.rpamis.architecture.template.AbstractBuildTemplate;
import cn.rpamis.architecture.template.TemplateFactory;
import cn.rpamis.architecture.utils.CfgUtils;
import cn.rpamis.common.dto.exception.ExceptionFactory;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * @author benym
 * @date 2022/7/20 4:48 下午
 */
@Service
public class BuildServiceImpl implements BuildService {


    private static final Logger logger = LoggerFactory.getLogger(BuildServiceImpl.class);

    @Autowired
    private TemplateFactory templateFactory;

    @Override
    public FileVO architectureBuild(BaseProjectConfig baseProjectConfig) {
        AbstractBuildTemplate template;
        try {
            template = templateFactory.getTemplate(baseProjectConfig.getTemplateType());
        } catch (Exception e) {
            throw ExceptionFactory.bizException("获取模板异常", e);
        }
        return template.createProject(baseProjectConfig);
    }

    @Override
    public void generate(File file, String templatesFtl, BaseProjectConfig baseProjectConfig) {
        try {
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
            }
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            CfgUtils.getCfg().getTemplate(templatesFtl, "UTF-8")
                    .process(baseProjectConfig, outputStreamWriter);
            outputStreamWriter.flush();
            outputStreamWriter.close();
        } catch (IOException | TemplateException e) {
            throw ExceptionFactory.bizException("文件生成异常", e);
        }
    }

    @Override
    public String zipProject(String artifactId, String buildId) {
        String genProjectPath =
                ProjectPath.CACHETEMP_PATH + buildId + File.separator + artifactId + File.separator;
        String saveZipPath = ProjectPath.CACHETEMP_PATH + buildId + File.separator + artifactId + ".zip";
        ZipUtil.zip(genProjectPath, saveZipPath);
        FileUtil.del(genProjectPath);
        return saveZipPath;
    }

    @Override
    public void download(String id) {
        String fileName;
        HttpServletResponse response = ((ServletRequestAttributes) Objects
                .requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
        synchronized (this) {
            try {
                List<String> list = FileUtil.listFileNames(ProjectPath.CACHETEMP_PATH + id);
                if (list.isEmpty()) {
                    throw ExceptionFactory.bizNoStackException("CACHE list为空");
                }
                fileName = list.get(0);
            } catch (Exception e) {
                throw ExceptionFactory.bizException("下载文件不存在", e);
            }
            try (ServletOutputStream outputStream = Objects.requireNonNull(response)
                    .getOutputStream()) {
                response.setContentType("application/x-download");
                response.addHeader("Content-Disposition",
                        "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
                String filePath = ProjectPath.CACHETEMP_PATH + id + File.separator + fileName;
                outputStream.write(FileUtil.readBytes(filePath));
                outputStream.flush();
            } catch (IOException e) {
                throw ExceptionFactory.bizException("打包文件异常", e);
            }
        }
    }
}

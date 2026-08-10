<template>
  <div class="landing-management">
    <div class="page-header">
      <div>
        <h1>落地页设置</h1>
        <p>以可视化方式管理公开站点品牌、首屏、栏目文案、投稿入口、社交链接和维护模式</p>
      </div>
      <el-button type="primary" :loading="saving" @click="saveSettings">
        <el-icon><Check /></el-icon>
        保存设置
      </el-button>
    </div>

    <el-tabs v-model="tab" class="landing-tabs">
      <el-tab-pane label="品牌与首屏" name="brand">
        <div class="settings-layout">
          <el-form label-position="top" class="settings-form">
            <section class="config-section">
              <div class="section-title">
                <el-icon><Monitor /></el-icon>
                <div>
                  <h2>站点识别</h2>
                  <p>控制公开落地页顶部品牌、浏览器标题和备用视觉资产。</p>
                </div>
              </div>
              <div class="field-grid two-cols">
                <el-form-item label="系统站点标题">
                  <el-input v-model="settings['site.title']" maxlength="120" placeholder="例如：融媒体管理系统" />
                </el-form-item>
                <el-form-item label="站点副标题">
                  <el-input v-model="settings['site.subtitle']" maxlength="120" placeholder="例如：Photography System" />
                </el-form-item>
                <el-form-item label="公开品牌名">
                  <el-input v-model="settings['landing.brand.title']" maxlength="80" placeholder="例如：融媒体中心" />
                </el-form-item>
                <el-form-item label="首屏备用背景">
                  <div class="media-control">
                    <div class="media-field">
                      <el-input v-model="settings['login.background']" placeholder="首屏媒体为空时会使用该背景" />
                      <el-upload
                        :show-file-list="false"
                        :http-request="options => uploadSettingMedia('login.background', options)"
                        accept="image/*"
                      >
                        <el-button :loading="isUploading('login.background')">
                          <el-icon><UploadFilled /></el-icon>
                          上传背景
                        </el-button>
                      </el-upload>
                    </div>
                    <div v-if="mediaPreview('login.background')" class="media-preview compact">
                      <img :src="mediaPreview('login.background')" alt="首屏备用背景预览" />
                      <el-button text type="danger" @click="clearSetting('login.background')">清除</el-button>
                    </div>
                  </div>
                </el-form-item>
              </div>
              <el-form-item label="站点 LOGO">
                <div class="media-control">
                  <div class="media-field">
                    <el-input v-model="settings['site.logo']" placeholder="上传后自动回填，也可填写图片地址" />
                    <el-upload
                      :show-file-list="false"
                      :http-request="options => uploadSettingMedia('site.logo', options)"
                      accept="image/*"
                    >
                      <el-button :loading="isUploading('site.logo')">
                        <el-icon><UploadFilled /></el-icon>
                        上传 LOGO
                      </el-button>
                    </el-upload>
                  </div>
                  <div v-if="mediaPreview('site.logo')" class="media-preview logo">
                    <img :src="mediaPreview('site.logo')" alt="站点 LOGO 预览" />
                    <div>
                      <strong>当前 LOGO</strong>
                      <span>{{ settings['site.logo'] }}</span>
                    </div>
                    <el-button text type="danger" @click="clearSetting('site.logo')">清除</el-button>
                  </div>
                </div>
              </el-form-item>
            </section>

            <section class="config-section">
              <div class="section-title">
                <el-icon><Camera /></el-icon>
                <div>
                  <h2>首屏内容</h2>
                  <p>控制用户进入公开站点后第一眼看到的标题、说明、按钮和图片/视频。</p>
                </div>
              </div>
              <div class="field-grid">
                <el-form-item label="首屏角标">
                  <el-input v-model="settings['landing.hero.badge']" maxlength="80" placeholder="例如：校园官方新媒体平台" />
                </el-form-item>
                <el-form-item label="首屏标题">
                  <el-input v-model="settings['landing.hero.title']" maxlength="160" placeholder="记录校园，让每一种声音被看见" />
                </el-form-item>
                <el-form-item label="首屏说明">
                  <el-input v-model="settings['landing.hero.subtitle']" type="textarea" :rows="3" maxlength="260" show-word-limit />
                </el-form-item>
                <div class="field-grid two-cols">
                  <el-form-item label="主按钮文案">
                    <el-input v-model="settings['landing.hero.primary_cta']" maxlength="30" placeholder="视频投稿" />
                  </el-form-item>
                  <el-form-item label="次按钮文案">
                    <el-input v-model="settings['landing.hero.secondary_cta']" maxlength="30" placeholder="了解我们" />
                  </el-form-item>
                </div>
                <el-form-item label="首屏媒体">
                  <div class="media-control">
                    <div class="media-field">
                      <el-input v-model="settings['landing.hero.media']" placeholder="上传图片或视频后自动回填" />
                      <el-upload
                        :show-file-list="false"
                        :http-request="uploadHero"
                        accept="image/*,video/mp4,video/quicktime,video/webm"
                      >
                        <el-button :loading="isUploading('landing.hero.media')">
                          <el-icon><UploadFilled /></el-icon>
                          上传图片或视频
                        </el-button>
                      </el-upload>
                    </div>
                    <div class="media-meta">
                      <el-segmented
                        v-model="settings['landing.hero.media_type']"
                        :options="[{ label: '图片', value: 'image' }, { label: '视频', value: 'video' }]"
                      />
                      <el-button v-if="settings['landing.hero.media']" text type="danger" @click="clearSetting('landing.hero.media')">清除媒体</el-button>
                    </div>
                  </div>
                </el-form-item>
              </div>
            </section>
          </el-form>

          <aside class="landing-preview">
            <div class="preview-heading">
              <span>首屏实时预览</span>
              <strong>{{ displaySetting('landing.brand.title', '融媒体中心') }}</strong>
            </div>
            <div class="preview-browser">
              <div class="preview-topbar">
                <span></span><span></span><span></span>
              </div>
              <div class="preview-hero">
                <video v-if="heroPreviewIsVideo && heroPreviewMedia" :src="heroPreviewMedia" autoplay muted loop playsinline />
                <img v-else-if="heroPreviewMedia" :src="heroPreviewMedia" alt="首屏媒体预览" />
                <div v-else class="preview-empty">
                  <el-icon><Picture /></el-icon>
                </div>
                <div class="preview-overlay"></div>
                <div class="preview-nav-row">
                  <span class="preview-logo">
                    <img v-if="logoPreview" :src="logoPreview" alt="" />
                    <el-icon v-else><Camera /></el-icon>
                  </span>
                  <span>{{ displaySetting('landing.nav.features_label', '校园特色') }}</span>
                  <span>{{ displaySetting('landing.nav.showcase_label', '部门风采') }}</span>
                </div>
                <div class="preview-hero-copy">
                  <small>{{ displaySetting('landing.hero.badge', '校园官方新媒体平台') }}</small>
                  <h3>{{ displaySetting('landing.hero.title', '记录校园，让每一种声音被看见') }}</h3>
                  <p>{{ displaySetting('landing.hero.subtitle', '校融媒体中心连接校园现场、青年创作与公共表达') }}</p>
                  <div>
                    <button type="button">{{ displaySetting('landing.hero.primary_cta', '视频投稿') }}</button>
                    <button type="button" class="ghost">{{ displaySetting('landing.hero.secondary_cta', '了解我们') }}</button>
                  </div>
                </div>
              </div>
            </div>
            <div class="preview-stat-grid">
              <div>
                <strong>{{ activeCampusCount }}</strong>
                <span>校园特色</span>
              </div>
              <div>
                <strong>{{ activeDepartmentCount }}</strong>
                <span>部门风采</span>
              </div>
              <div>
                <strong>{{ settings['landing.hero.media_type'] === 'video' ? '视频' : '图片' }}</strong>
                <span>首屏媒体</span>
              </div>
            </div>
          </aside>
        </div>
      </el-tab-pane>

      <el-tab-pane label="内容文案" name="copy">
        <div class="settings-layout">
          <el-form label-position="top" class="settings-form">
            <section class="config-section">
              <div class="section-title">
                <el-icon><Link /></el-icon>
                <div>
                  <h2>导航入口</h2>
                  <p>配置公开落地页顶部导航的可见文案。</p>
                </div>
              </div>
              <div class="field-grid four-cols">
                <el-form-item label="首页">
                  <el-input v-model="settings['landing.nav.home_label']" maxlength="20" />
                </el-form-item>
                <el-form-item label="校园特色">
                  <el-input v-model="settings['landing.nav.features_label']" maxlength="20" />
                </el-form-item>
                <el-form-item label="部门风采">
                  <el-input v-model="settings['landing.nav.showcase_label']" maxlength="20" />
                </el-form-item>
                <el-form-item label="投稿入口">
                  <el-input v-model="settings['landing.nav.submission_label']" maxlength="20" />
                </el-form-item>
              </div>
            </section>

            <section class="config-section">
              <div class="section-title">
                <el-icon><Reading /></el-icon>
                <div>
                  <h2>校园特色栏目</h2>
                  <p>配置校园特色卡片区上方的栏目说明。</p>
                </div>
              </div>
              <div class="field-grid">
                <el-form-item label="栏目角标">
                  <el-input v-model="settings['landing.features.eyebrow']" maxlength="40" />
                </el-form-item>
                <el-form-item label="栏目标题">
                  <el-input v-model="settings['landing.features.title']" maxlength="80" />
                </el-form-item>
                <el-form-item label="栏目说明">
                  <el-input v-model="settings['landing.features.description']" type="textarea" :rows="3" maxlength="220" show-word-limit />
                </el-form-item>
              </div>
            </section>

            <section class="config-section">
              <div class="section-title">
                <el-icon><OfficeBuilding /></el-icon>
                <div>
                  <h2>部门风采栏目</h2>
                  <p>配置部门轮播和卡片区上方的栏目说明。</p>
                </div>
              </div>
              <div class="field-grid">
                <el-form-item label="栏目角标">
                  <el-input v-model="settings['landing.showcase.eyebrow']" maxlength="40" />
                </el-form-item>
                <el-form-item label="栏目标题">
                  <el-input v-model="settings['landing.showcase.title']" maxlength="80" />
                </el-form-item>
                <el-form-item label="栏目说明">
                  <el-input v-model="settings['landing.showcase.description']" type="textarea" :rows="3" maxlength="220" show-word-limit />
                </el-form-item>
              </div>
            </section>
          </el-form>

          <aside class="landing-preview copy-preview">
            <div class="preview-heading">
              <span>栏目预览</span>
              <strong>公开内容结构</strong>
            </div>
            <div class="section-preview-block">
              <small>{{ displaySetting('landing.features.eyebrow', '校园特色') }}</small>
              <h3>{{ displaySetting('landing.features.title', '发现不一样的校园') }}</h3>
              <p>{{ displaySetting('landing.features.description', '在这里，每一个角落都有故事，每一刻时光都值得被记录') }}</p>
              <div class="mini-card-grid">
                <div v-for="item in campusPreviewItems" :key="item.id || item.title">
                  <span></span>
                  <strong>{{ item.title }}</strong>
                </div>
              </div>
            </div>
            <div class="section-preview-block dark">
              <small>{{ displaySetting('landing.showcase.eyebrow', '部门风采') }}</small>
              <h3>{{ displaySetting('landing.showcase.title', '我们的故事') }}</h3>
              <p>{{ displaySetting('landing.showcase.description', '一群热爱影像与创作的年轻人，用镜头讲述校园里的每一个精彩瞬间。') }}</p>
              <div class="mini-card-grid">
                <div v-for="item in departmentPreviewItems" :key="item.id || item.title">
                  <span></span>
                  <strong>{{ item.title }}</strong>
                </div>
              </div>
            </div>
          </aside>
        </div>
      </el-tab-pane>

      <el-tab-pane label="投稿与页脚" name="footer">
        <div class="settings-layout">
          <el-form label-position="top" class="settings-form">
            <section class="config-section">
              <div class="section-title">
                <el-icon><VideoCamera /></el-icon>
                <div>
                  <h2>投稿区</h2>
                  <p>配置视频投稿区标题、按钮、流程和投稿方向。</p>
                </div>
              </div>
              <div class="field-grid">
                <el-form-item label="投稿区角标">
                  <el-input v-model="settings['landing.submission.eyebrow']" maxlength="40" />
                </el-form-item>
                <el-form-item label="投稿区标题">
                  <el-input v-model="settings['landing.submission.title']" maxlength="100" />
                </el-form-item>
                <el-form-item label="投稿区说明">
                  <el-input v-model="settings['landing.submission.description']" type="textarea" :rows="3" maxlength="220" show-word-limit />
                </el-form-item>
                <div class="field-grid three-cols">
                  <el-form-item label="主按钮文案">
                    <el-input v-model="settings['landing.submission.primary_cta']" maxlength="30" />
                  </el-form-item>
                  <el-form-item label="次按钮文案">
                    <el-input v-model="settings['landing.submission.secondary_cta']" maxlength="30" />
                  </el-form-item>
                  <el-form-item label="视觉角标">
                    <el-input v-model="settings['landing.submission.visual_badge']" maxlength="30" />
                  </el-form-item>
                </div>
              </div>

              <div class="sub-config-title">投稿流程</div>
              <div class="field-grid three-cols">
                <el-form-item label="步骤 01">
                  <el-input v-model="settings['landing.submission.step_one']" maxlength="30" />
                </el-form-item>
                <el-form-item label="步骤 02">
                  <el-input v-model="settings['landing.submission.step_two']" maxlength="30" />
                </el-form-item>
                <el-form-item label="步骤 03">
                  <el-input v-model="settings['landing.submission.step_three']" maxlength="30" />
                </el-form-item>
              </div>

              <div class="sub-config-title">投稿方向</div>
              <div class="topic-config-grid">
                <div class="topic-editor">
                  <span>方向 01</span>
                  <el-input v-model="settings['landing.submission.topic_one.title']" maxlength="30" placeholder="标题" />
                  <el-input v-model="settings['landing.submission.topic_one.summary']" maxlength="80" placeholder="说明" />
                </div>
                <div class="topic-editor">
                  <span>方向 02</span>
                  <el-input v-model="settings['landing.submission.topic_two.title']" maxlength="30" placeholder="标题" />
                  <el-input v-model="settings['landing.submission.topic_two.summary']" maxlength="80" placeholder="说明" />
                </div>
                <div class="topic-editor">
                  <span>方向 03</span>
                  <el-input v-model="settings['landing.submission.topic_three.title']" maxlength="30" placeholder="标题" />
                  <el-input v-model="settings['landing.submission.topic_three.summary']" maxlength="80" placeholder="说明" />
                </div>
              </div>
            </section>

            <section class="config-section">
              <div class="section-title">
                <el-icon><Message /></el-icon>
                <div>
                  <h2>页脚与联系方式</h2>
                  <p>配置公开页底部说明、联系信息、社交链接和二维码。</p>
                </div>
              </div>
              <div class="field-grid">
                <el-form-item label="页脚说明">
                  <el-input v-model="settings['landing.footer.description']" type="textarea" :rows="3" maxlength="260" show-word-limit />
                </el-form-item>
                <div class="field-grid two-cols">
                  <el-form-item label="社交标题">
                    <el-input v-model="settings['landing.footer.social_title']" maxlength="30" />
                  </el-form-item>
                  <el-form-item label="联系标题">
                    <el-input v-model="settings['landing.footer.contact_title']" maxlength="30" />
                  </el-form-item>
                </div>
                <div class="field-grid three-cols">
                  <el-form-item label="邮箱">
                    <el-input v-model="settings['landing.contact.email']" maxlength="120" placeholder="media@campus.edu.cn" />
                  </el-form-item>
                  <el-form-item label="电话">
                    <el-input v-model="settings['landing.contact.phone']" maxlength="40" placeholder="010-12345678" />
                  </el-form-item>
                  <el-form-item label="地址">
                    <el-input v-model="settings['landing.contact.address']" maxlength="120" placeholder="行政楼 203 室" />
                  </el-form-item>
                </div>
                <el-form-item label="联系备注（兼容旧字段）">
                  <el-input v-model="settings['landing.contact']" maxlength="160" placeholder="用于旧版联系方式或补充说明" />
                </el-form-item>
                <div class="field-grid two-cols">
                  <el-form-item label="抖音链接">
                    <el-input v-model="settings['landing.social.douyin_url']" placeholder="https://..." />
                  </el-form-item>
                  <el-form-item label="微信公众号链接">
                    <el-input v-model="settings['landing.social.wechat_url']" placeholder="https://..." />
                  </el-form-item>
                </div>
                <el-form-item label="学校官网链接">
                  <el-input v-model="settings['landing.social.website_url']" placeholder="https://school.edu.cn" />
                </el-form-item>
                <el-form-item label="微信二维码">
                  <div class="media-control">
                    <div class="media-field">
                      <el-input v-model="settings['landing.social.wechat_qr']" placeholder="上传二维码后自动回填，也可填写图片地址" />
                      <el-upload
                        :show-file-list="false"
                        :http-request="options => uploadSettingMedia('landing.social.wechat_qr', options)"
                        accept="image/*"
                      >
                        <el-button :loading="isUploading('landing.social.wechat_qr')">
                          <el-icon><UploadFilled /></el-icon>
                          上传二维码
                        </el-button>
                      </el-upload>
                    </div>
                    <div v-if="mediaPreview('landing.social.wechat_qr')" class="media-preview qr">
                      <img :src="mediaPreview('landing.social.wechat_qr')" alt="微信二维码预览" />
                      <el-button text type="danger" @click="clearSetting('landing.social.wechat_qr')">清除</el-button>
                    </div>
                  </div>
                </el-form-item>
                <el-form-item label="版权后缀">
                  <el-input v-model="settings['landing.footer.copyright_suffix']" maxlength="40" placeholder="版权所有" />
                </el-form-item>
              </div>
            </section>
          </el-form>

          <aside class="landing-preview submit-preview">
            <div class="preview-heading">
              <span>投稿与页脚预览</span>
              <strong>{{ displaySetting('landing.submission.eyebrow', '视频投稿') }}</strong>
            </div>
            <div class="submit-preview-card">
              <small>{{ displaySetting('landing.submission.eyebrow', '视频投稿') }}</small>
              <h3>{{ displaySetting('landing.submission.title', '把你的校园故事交给我们') }}</h3>
              <p>{{ displaySetting('landing.submission.description', '支持校园新闻、人物、活动与创意短视频投稿') }}</p>
              <div class="preview-actions">
                <button type="button">{{ displaySetting('landing.submission.primary_cta', '开始投稿') }}</button>
                <button type="button">{{ displaySetting('landing.submission.secondary_cta', '部门风采') }}</button>
              </div>
              <div class="preview-step-row">
                <span v-for="step in submissionStepPreview" :key="step.index">{{ step.index }} {{ step.title }}</span>
              </div>
            </div>
            <div class="footer-preview-card">
              <strong>{{ displaySetting('landing.footer.contact_title', '联系我们') }}</strong>
              <p>{{ displaySetting('landing.contact.email', 'media@campus.edu.cn') }}</p>
              <p>{{ displaySetting('landing.contact.phone', '010-12345678') }}</p>
              <p>{{ displaySetting('landing.contact.address', displaySetting('landing.contact', '行政楼 203 室')) }}</p>
              <img v-if="mediaPreview('landing.social.wechat_qr')" :src="mediaPreview('landing.social.wechat_qr')" alt="微信二维码预览" />
            </div>
          </aside>
        </div>
      </el-tab-pane>

      <el-tab-pane label="校园特色" name="campus">
        <content-table type="CAMPUS_FEATURE" :items="campusItems" @changed="load" />
      </el-tab-pane>

      <el-tab-pane label="部门风采" name="departments">
        <content-table type="DEPARTMENT_SHOWCASE" :items="departmentItems" @changed="load" />
      </el-tab-pane>

      <el-tab-pane label="维护模式" name="maintenance">
        <el-form label-position="top" class="maintenance-form">
          <section class="config-section">
            <div class="section-title">
              <el-icon><Lock /></el-icon>
              <div>
                <h2>维护模式</h2>
                <p>临时关闭公开访问，并展示维护说明；管理员可使用维护密码进入。</p>
              </div>
            </div>
            <div class="field-grid">
              <el-form-item label="维护状态">
                <el-switch v-model="maintenance.enabled" active-text="开启维护" inactive-text="正常开放" />
              </el-form-item>
              <el-form-item label="维护页标题">
                <el-input v-model="maintenance.title" maxlength="100" />
              </el-form-item>
              <el-form-item label="维护说明">
                <el-input v-model="maintenance.message" type="textarea" :rows="4" maxlength="260" show-word-limit />
              </el-form-item>
              <el-form-item label="维护密码">
                <el-input v-model="maintenance.password" type="password" show-password placeholder="留空表示不修改，首次开启至少8位" />
              </el-form-item>
            </div>
            <el-button type="primary" @click="saveMaintenance">保存维护设置</el-button>
          </section>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, reactive, ref } from 'vue'
import {
  Camera,
  Check,
  Link,
  Lock,
  Message,
  Monitor,
  OfficeBuilding,
  Picture,
  Reading,
  UploadFilled,
  VideoCamera
} from '@element-plus/icons-vue'
import {
  ElButton,
  ElDialog,
  ElForm,
  ElFormItem,
  ElIcon,
  ElImage,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElRadioButton,
  ElRadioGroup,
  ElSlider,
  ElSwitch,
  ElTable,
  ElTableColumn,
  ElTag,
  ElUpload
} from 'element-plus'
import request from '@/utils/request'
import { getSiteImageUrl } from '@/utils/imageUrl'

const settingDefaults = {
  'site.title': '融媒体管理系统',
  'site.subtitle': 'Photography System',
  'site.logo': '',
  'login.background': '',
  'landing.brand.title': '融媒体中心',
  'landing.nav.home_label': '首页',
  'landing.nav.features_label': '校园特色',
  'landing.nav.showcase_label': '部门风采',
  'landing.nav.submission_label': '视频投稿',
  'landing.hero.badge': '校园官方新媒体平台',
  'landing.hero.title': '记录校园，让每一种声音被看见',
  'landing.hero.subtitle': '校融媒体中心连接校园现场、青年创作与公共表达',
  'landing.hero.primary_cta': '视频投稿',
  'landing.hero.secondary_cta': '了解我们',
  'landing.hero.media': '',
  'landing.hero.media_type': 'image',
  'landing.features.eyebrow': '校园特色',
  'landing.features.title': '发现不一样的校园',
  'landing.features.description': '在这里，每一个角落都有故事，每一刻时光都值得被记录',
  'landing.showcase.eyebrow': '部门风采',
  'landing.showcase.title': '我们的故事',
  'landing.showcase.description': '一群热爱影像与创作的年轻人，用镜头讲述校园里的每一个精彩瞬间。',
  'landing.submission.eyebrow': '视频投稿',
  'landing.submission.title': '把你的校园故事交给我们',
  'landing.submission.description': '支持校园新闻、人物、活动与创意短视频投稿',
  'landing.submission.primary_cta': '开始投稿',
  'landing.submission.secondary_cta': '部门风采',
  'landing.submission.visual_badge': '校园影像库',
  'landing.submission.step_one': '上传素材',
  'landing.submission.step_two': '填写信息',
  'landing.submission.step_three': '等待审核',
  'landing.submission.topic_one.title': '校园新闻',
  'landing.submission.topic_one.summary': '记录现场与公共议题',
  'landing.submission.topic_two.title': '人物故事',
  'landing.submission.topic_two.summary': '呈现青春里的闪光时刻',
  'landing.submission.topic_three.title': '活动创意',
  'landing.submission.topic_three.summary': '捕捉舞台、社团与灵感',
  'landing.footer.description': '用镜头记录青春，用创意点亮校园。我们是校园里的记录者，用影像传递温度与力量。',
  'landing.footer.social_title': '关注我们',
  'landing.footer.contact_title': '联系我们',
  'landing.footer.copyright_suffix': '版权所有',
  'landing.contact.email': 'media@campus.edu.cn',
  'landing.contact.phone': '010-12345678',
  'landing.contact.address': '行政楼 203 室',
  'landing.contact': '',
  'landing.social.douyin_url': '',
  'landing.social.wechat_url': '',
  'landing.social.wechat_qr': '',
  'landing.social.website_url': 'https://www.campus.edu.cn'
}

const tab = ref('brand')
const items = ref([])
const saving = ref(false)
const uploadingKey = ref('')
const settings = reactive({ ...settingDefaults })
const maintenance = reactive({ enabled: false, title: '', message: '', password: '' })

const campusItems = computed(() => items.value.filter(item => item.sectionType === 'CAMPUS_FEATURE'))
const departmentItems = computed(() => items.value.filter(item => item.sectionType === 'DEPARTMENT_SHOWCASE'))
const activeCampusCount = computed(() => campusItems.value.filter(item => item.published !== false).length)
const activeDepartmentCount = computed(() => departmentItems.value.filter(item => item.published !== false).length)
const campusPreviewItems = computed(() => campusItems.value.slice(0, 3))
const departmentPreviewItems = computed(() => departmentItems.value.slice(0, 3))
const logoPreview = computed(() => mediaPreview('site.logo'))
const heroPreviewMedia = computed(() => mediaPreview('landing.hero.media') || mediaPreview('login.background'))
const heroPreviewIsVideo = computed(() => {
  const media = heroPreviewMedia.value
  return settings['landing.hero.media_type'] === 'video' || /\.(mp4|mov|webm)(\?|$)/i.test(media)
})
const submissionStepPreview = computed(() => [
  { index: '01', title: displaySetting('landing.submission.step_one', '上传素材') },
  { index: '02', title: displaySetting('landing.submission.step_two', '填写信息') },
  { index: '03', title: displaySetting('landing.submission.step_three', '等待审核') }
])

const displaySetting = (key, fallback = '') => settings[key] || fallback
const mediaPreview = key => getSiteImageUrl(settings[key]) || ''
const isUploading = key => uploadingKey.value === key
const clearSetting = key => { settings[key] = '' }

const settingsPayload = () => {
  return Object.keys(settingDefaults).reduce((payload, key) => {
    payload[key] = settings[key] ?? ''
    return payload
  }, {})
}

const load = async () => {
  const [publicData, itemData, status] = await Promise.all([
    request.get('/landing/public'),
    request.get('/landing/admin/items'),
    request.get('/maintenance/public/status')
  ])
  Object.assign(settings, settingDefaults, publicData.data?.settings || {})
  items.value = itemData.data || []
  Object.assign(maintenance, status.data || {}, { password: '' })
}

const saveSettings = async () => {
  saving.value = true
  try {
    const response = await request.put('/landing/admin/settings', settingsPayload())
    Object.assign(settings, settingDefaults, response.data || {})
    ElMessage.success('落地页设置已保存')
  } finally {
    saving.value = false
  }
}

const uploadSettingMedia = async (key, options) => {
  uploadingKey.value = key
  try {
    const data = new FormData()
    data.append('file', options.file)
    const response = await request.post('/landing/admin/media', data, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 10 * 60 * 1000
    })
    settings[key] = response.data
    if (key === 'landing.hero.media') {
      settings['landing.hero.media_type'] = options.file.type.startsWith('video/') ? 'video' : 'image'
    }
    ElMessage.success('媒体已上传，请保存设置')
  } finally {
    uploadingKey.value = ''
  }
}

const uploadHero = options => uploadSettingMedia('landing.hero.media', options)

const uploadLandingMedia = async file => {
  const data = new FormData()
  data.append('file', file)
  const response = await request.post('/landing/admin/media', data, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 10 * 60 * 1000
  })
  return response.data
}

const outputSize = aspect => aspect === '16:9'
  ? { width: 1600, height: 900 }
  : { width: 1200, height: 900 }

const createImage = src => new Promise((resolve, reject) => {
  const image = new Image()
  image.onload = () => resolve(image)
  image.onerror = () => reject(new Error('图片读取失败，请重新选择'))
  image.src = src
})

const cropImageFile = async crop => {
  const image = await createImage(crop.sourceUrl)
  const { width, height } = outputSize(crop.aspect)
  const canvas = document.createElement('canvas')
  const context = canvas.getContext('2d')
  canvas.width = width
  canvas.height = height

  const baseScale = Math.max(width / image.naturalWidth, height / image.naturalHeight)
  const scale = baseScale * crop.zoom
  const drawWidth = image.naturalWidth * scale
  const drawHeight = image.naturalHeight * scale
  const maxOffsetX = Math.max(0, drawWidth - width)
  const maxOffsetY = Math.max(0, drawHeight - height)
  const drawX = -maxOffsetX * ((crop.offsetX + 1) / 2)
  const drawY = -maxOffsetY * ((crop.offsetY + 1) / 2)

  context.fillStyle = '#f6fbff'
  context.fillRect(0, 0, width, height)
  context.drawImage(image, drawX, drawY, drawWidth, drawHeight)

  const blob = await new Promise((resolve, reject) => {
    canvas.toBlob(value => value ? resolve(value) : reject(new Error('图片裁剪失败')), 'image/jpeg', 0.92)
  })
  const fileName = `${(crop.file?.name || 'landing-image').replace(/\.[^.]+$/, '')}-cropped.jpg`
  return new File([blob], fileName, { type: 'image/jpeg' })
}

const saveMaintenance = async () => {
  await request.put('/maintenance/admin/settings', maintenance)
  maintenance.password = ''
  ElMessage.success('维护设置已保存')
}

const ContentTable = defineComponent({
  props: { type: String, items: Array },
  emits: ['changed'],
  setup(props, { emit }) {
    const visible = ref(false)
    const currentId = ref(null)
    const form = reactive({ title: '', summary: '', mediaUrl: '', linkUrl: '', published: true, sortOrder: 0 })
    const crop = reactive({
      visible: false,
      uploading: false,
      sourceUrl: '',
      file: null,
      aspect: '4:3',
      zoom: 1,
      offsetX: 0,
      offsetY: 0,
      dragging: false,
      dragStartX: 0,
      dragStartY: 0,
      startOffsetX: 0,
      startOffsetY: 0
    })
    const titleText = computed(() => props.type === 'DEPARTMENT_SHOWCASE' ? '部门风采' : '校园特色')
    const recommendedAspect = computed(() => props.type === 'DEPARTMENT_SHOWCASE' ? '16:9' : '4:3')
    const recommendedText = computed(() => props.type === 'DEPARTMENT_SHOWCASE' ? '部门风采建议 16:9，适合轮播展示。' : '校园特色建议 4:3，适合卡片展示。')
    const edit = item => {
      currentId.value = item?.id || null
      Object.assign(form, {
        title: item?.title || '',
        summary: item?.summary || '',
        mediaUrl: item?.mediaUrl || '',
        linkUrl: item?.linkUrl || '',
        published: item?.published ?? true,
        sortOrder: item?.sortOrder || 0
      })
      visible.value = true
    }
    const save = async () => {
      const body = { ...form, sectionType: props.type }
      currentId.value ? await request.put(`/landing/admin/items/${currentId.value}`, body) : await request.post('/landing/admin/items', body)
      visible.value = false
      ElMessage.success('内容已保存')
      emit('changed')
    }
    const remove = async id => {
      await ElMessageBox.confirm('确认删除这条内容？', '删除确认')
      await request.delete(`/landing/admin/items/${id}`)
      ElMessage.success('内容已删除')
      emit('changed')
    }
    const resetCrop = () => {
      if (crop.sourceUrl) URL.revokeObjectURL(crop.sourceUrl)
      Object.assign(crop, {
        visible: false,
        uploading: false,
        sourceUrl: '',
        file: null,
        aspect: recommendedAspect.value,
        zoom: 1,
        offsetX: 0,
        offsetY: 0,
        dragging: false,
        dragStartX: 0,
        dragStartY: 0,
        startOffsetX: 0,
        startOffsetY: 0
      })
    }
    const selectImage = options => {
      const file = options.file
      if (!file?.type?.startsWith('image/')) {
        ElMessage.warning('请选择 JPG、PNG 或 WebP 图片')
        options.onError?.(new Error('请选择图片'))
        return
      }
      if (file.size > 20 * 1024 * 1024) {
        ElMessage.warning('图片不能超过 20MB')
        options.onError?.(new Error('图片过大'))
        return
      }
      resetCrop()
      crop.file = file
      crop.sourceUrl = URL.createObjectURL(file)
      crop.aspect = recommendedAspect.value
      crop.visible = true
      options.onSuccess?.({})
    }
    const submitCrop = async () => {
      crop.uploading = true
      try {
        const croppedFile = await cropImageFile(crop)
        form.mediaUrl = await uploadLandingMedia(croppedFile)
        ElMessage.success('图片已裁剪并上传，请保存内容')
        resetCrop()
      } finally {
        crop.uploading = false
      }
    }
    const clamp = value => Math.min(1, Math.max(-1, value))
    const point = event => event.touches?.[0] || event
    const startDrag = event => {
      const current = point(event)
      crop.dragging = true
      crop.dragStartX = current.clientX
      crop.dragStartY = current.clientY
      crop.startOffsetX = crop.offsetX
      crop.startOffsetY = crop.offsetY
      event.preventDefault()
    }
    const moveDrag = event => {
      if (!crop.dragging) return
      const current = point(event)
      crop.offsetX = clamp(crop.startOffsetX + (current.clientX - crop.dragStartX) / 220)
      crop.offsetY = clamp(crop.startOffsetY + (current.clientY - crop.dragStartY) / 160)
      event.preventDefault()
    }
    const endDrag = () => { crop.dragging = false }
    return () => h('div', { class: 'content-editor' }, [
      h('div', { class: 'content-toolbar' }, [
        h('div', null, [
          h('h2', `${titleText.value}内容`),
          h('p', '管理公开落地页中的图片卡片、排序、发布状态和跳转链接。')
        ]),
        h(ElButton, { type: 'primary', onClick: () => edit(null) }, () => '新增内容')
      ]),
      h(ElTable, { data: props.items, style: 'margin-top:16px' }, () => [
        h(ElTableColumn, { label: '媒体', width: 112 }, {
          default: ({ row }) => row.mediaUrl
            ? h(ElImage, { src: getSiteImageUrl(row.mediaUrl), fit: 'cover', previewSrcList: [getSiteImageUrl(row.mediaUrl)], hideOnClickModal: true, class: 'table-media-thumb' })
            : h('div', { class: 'table-media-empty' }, [h(ElIcon, null, () => h(Picture))])
        }),
        h(ElTableColumn, { prop: 'title', label: '标题', minWidth: 150 }),
        h(ElTableColumn, { prop: 'summary', label: '说明', minWidth: 260, showOverflowTooltip: true }),
        h(ElTableColumn, { label: '状态', width: 90 }, {
          default: ({ row }) => h(ElTag, { type: row.published === false ? 'info' : 'success', effect: 'light' }, () => row.published === false ? '隐藏' : '发布')
        }),
        h(ElTableColumn, { prop: 'sortOrder', label: '排序', width: 80 }),
        h(ElTableColumn, { label: '操作', width: 150 }, {
          default: ({ row }) => [
            h(ElButton, { text: true, type: 'primary', onClick: () => edit(row) }, () => '编辑'),
            h(ElButton, { text: true, type: 'danger', onClick: () => remove(row.id) }, () => '删除')
          ]
        })
      ]),
      h(ElDialog, { modelValue: visible.value, 'onUpdate:modelValue': value => visible.value = value, title: currentId.value ? '编辑内容' : '新增内容', width: 'min(680px, 92vw)' }, {
        default: () => h(ElForm, { labelPosition: 'top' }, () => [
          h(ElFormItem, { label: '标题' }, () => h(ElInput, { modelValue: form.title, maxlength: 160, showWordLimit: true, 'onUpdate:modelValue': value => { form.title = value } })),
          h(ElFormItem, { label: '说明' }, () => h(ElInput, { modelValue: form.summary, type: 'textarea', rows: 4, maxlength: 260, showWordLimit: true, 'onUpdate:modelValue': value => { form.summary = value } })),
          h('div', { class: 'dialog-field-grid' }, [
            h(ElFormItem, { label: '排序' }, () => h(ElInputNumber, { modelValue: form.sortOrder, min: 0, step: 1, controlsPosition: 'right', 'onUpdate:modelValue': value => { form.sortOrder = value ?? 0 } })),
            h(ElFormItem, { label: '发布状态' }, () => h(ElSwitch, { modelValue: form.published, activeText: '发布', inactiveText: '隐藏', 'onUpdate:modelValue': value => { form.published = value } }))
          ]),
          h(ElFormItem, { label: '跳转链接' }, () => h(ElInput, { modelValue: form.linkUrl, placeholder: '可选：填写后用于后续扩展跳转', 'onUpdate:modelValue': value => { form.linkUrl = value } })),
          h(ElFormItem, { label: '媒体地址' }, () => h('div', { class: 'content-media-control' }, [
            h('div', { class: 'media-field' }, [
              h(ElInput, { modelValue: form.mediaUrl, placeholder: '可粘贴图片地址，或上传后自动回填', 'onUpdate:modelValue': value => { form.mediaUrl = value } }),
              h(ElUpload, { showFileList: false, accept: 'image/jpeg,image/png,image/webp', httpRequest: selectImage }, {
                default: () => h(ElButton, { type: 'primary', plain: true, loading: crop.uploading }, () => [
                  h(ElIcon, null, () => h(UploadFilled)),
                  '上传并裁剪'
                ])
              })
            ]),
            h('p', { class: 'media-hint' }, recommendedText.value),
            form.mediaUrl ? h('div', { class: 'media-preview-row' }, [
              h(ElImage, { src: getSiteImageUrl(form.mediaUrl), fit: 'cover', previewSrcList: [getSiteImageUrl(form.mediaUrl)], hideOnClickModal: true, class: 'media-preview-image' }),
              h(ElButton, { text: true, type: 'danger', onClick: () => { form.mediaUrl = '' } }, () => '清除图片')
            ]) : null
          ]))
        ]),
        footer: () => [
          h(ElButton, { onClick: () => { visible.value = false } }, () => '取消'),
          h(ElButton, { type: 'primary', onClick: save }, () => '保存')
        ]
      }),
      h(ElDialog, { modelValue: crop.visible, 'onUpdate:modelValue': value => { if (!value) resetCrop() }, title: '裁剪图片', width: 'min(760px, 94vw)', closeOnClickModal: false, destroyOnClose: true }, {
        default: () => h('div', { class: 'crop-editor' }, [
          h('div', {
            class: ['crop-stage', crop.dragging ? 'is-dragging' : ''],
            style: { aspectRatio: crop.aspect === '16:9' ? '16 / 9' : '4 / 3' },
            onMousedown: startDrag,
            onMousemove: moveDrag,
            onMouseup: endDrag,
            onMouseleave: endDrag,
            onTouchstart: startDrag,
            onTouchmove: moveDrag,
            onTouchend: endDrag
          }, [
            crop.sourceUrl ? h('img', {
              src: crop.sourceUrl,
              alt: '裁剪预览',
              draggable: false,
              style: {
                objectPosition: `${50 + crop.offsetX * 50}% ${50 + crop.offsetY * 50}%`,
                transform: `scale(${crop.zoom})`
              }
            }) : h('div', { class: 'crop-empty' }, [h(ElIcon, null, () => h(Picture)), '请选择图片']),
            h('div', { class: 'crop-guide' }, '拖拽图片调整主体位置')
          ]),
          h('div', { class: 'crop-controls' }, [
            h('div', { class: 'crop-control-row' }, [
              h('span', '裁剪比例'),
              h(ElRadioGroup, { modelValue: crop.aspect, 'onUpdate:modelValue': value => { crop.aspect = value; crop.offsetX = 0; crop.offsetY = 0 } }, () => [
                h(ElRadioButton, { label: '4:3' }, () => '卡片 4:3'),
                h(ElRadioButton, { label: '16:9' }, () => '轮播 16:9')
              ])
            ]),
            h('label', { class: 'crop-slider' }, [
              h('span', '缩放'),
              h(ElSlider, { modelValue: crop.zoom, min: 1, max: 2.4, step: 0.05, 'onUpdate:modelValue': value => { crop.zoom = value } })
            ]),
            h('label', { class: 'crop-slider' }, [
              h('span', '水平位置'),
              h(ElSlider, { modelValue: crop.offsetX, min: -1, max: 1, step: 0.01, 'onUpdate:modelValue': value => { crop.offsetX = value } })
            ]),
            h('label', { class: 'crop-slider' }, [
              h('span', '垂直位置'),
              h(ElSlider, { modelValue: crop.offsetY, min: -1, max: 1, step: 0.01, 'onUpdate:modelValue': value => { crop.offsetY = value } })
            ])
          ])
        ]),
        footer: () => [
          h(ElButton, { onClick: resetCrop }, () => '取消'),
          h(ElButton, { type: 'primary', loading: crop.uploading, onClick: submitCrop }, () => '确认裁剪并上传')
        ]
      })
    ])
  }
})

onMounted(load)
</script>

<style scoped>
.landing-management {
  padding: 4px;
}

.page-header {
  min-height: 130px;
  padding: 28px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(234, 253, 250, 0.78)),
    #fff;
  border: 1px solid rgba(188, 223, 235, 0.68);
  border-radius: 8px;
  box-shadow: 0 18px 48px rgba(50, 128, 160, 0.08);
}

.page-header h1 {
  margin: 0;
  color: #12384f;
  font-size: 28px;
  line-height: 1.25;
  font-weight: 900;
  letter-spacing: 0;
}

.page-header p {
  margin: 8px 0 0;
  color: #637f90;
  font-size: 15px;
  line-height: 1.6;
}

.landing-tabs {
  margin-top: 22px;
  padding: 22px 26px 26px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #dce8ed;
  border-radius: 8px;
  box-shadow: 0 16px 40px rgba(36, 108, 142, 0.08);
}

.settings-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 430px);
  gap: 24px;
  align-items: start;
}

.settings-form,
.maintenance-form {
  min-width: 0;
}

.config-section {
  padding: 22px;
  background: linear-gradient(180deg, #fff, #fbfeff);
  border: 1px solid rgba(202, 222, 230, 0.9);
  border-radius: 8px;
}

.config-section + .config-section {
  margin-top: 18px;
}

.section-title {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
}

.section-title > .el-icon {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  color: #0f8fc3;
  font-size: 22px;
  background: linear-gradient(135deg, #e7f8ff, #ecfff8);
  border: 1px solid rgba(34, 184, 211, 0.18);
  border-radius: 8px;
}

.section-title h2,
.content-toolbar h2 {
  margin: 0;
  color: #173f56;
  font-size: 18px;
  line-height: 1.32;
  font-weight: 900;
  letter-spacing: 0;
}

.section-title p,
.content-toolbar p {
  margin: 4px 0 0;
  color: #6a808b;
  font-size: 13px;
  line-height: 1.5;
}

.field-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 2px 16px;
}

.field-grid.two-cols {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.field-grid.three-cols {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.field-grid.four-cols {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.media-control {
  width: 100%;
  display: grid;
  gap: 10px;
}

.media-field {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
}

.media-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.media-preview {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5fbff;
  border: 1px solid #dce8ed;
  border-radius: 8px;
}

.media-preview.compact,
.media-preview.qr {
  grid-template-columns: auto auto;
  justify-content: start;
}

.media-preview img {
  width: 128px;
  height: 78px;
  object-fit: cover;
  border-radius: 6px;
  background: #eaf6fb;
}

.media-preview.logo img,
.media-preview.qr img {
  width: 76px;
  height: 76px;
  object-fit: contain;
  background: #fff;
  border: 1px solid rgba(188, 223, 235, 0.8);
}

.media-preview strong {
  display: block;
  color: #173f56;
  font-size: 13px;
  line-height: 1.4;
}

.media-preview span {
  display: block;
  overflow: hidden;
  color: #6a808b;
  font-size: 12px;
  line-height: 1.5;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.landing-preview {
  position: sticky;
  top: 20px;
  min-width: 0;
  display: grid;
  gap: 14px;
}

.preview-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #173f56;
}

.preview-heading span {
  color: #6a808b;
  font-size: 13px;
  font-weight: 800;
}

.preview-heading strong {
  overflow: hidden;
  font-size: 15px;
  line-height: 1.3;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-browser,
.section-preview-block,
.submit-preview-card,
.footer-preview-card {
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(188, 223, 235, 0.82);
  border-radius: 8px;
  box-shadow: 0 14px 34px rgba(31, 99, 132, 0.1);
}

.preview-topbar {
  height: 34px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  gap: 7px;
  background: #f1f8fb;
  border-bottom: 1px solid rgba(188, 223, 235, 0.72);
}

.preview-topbar span {
  width: 8px;
  height: 8px;
  background: #9edbe9;
  border-radius: 999px;
}

.preview-topbar span:nth-child(2) {
  background: #ffd58a;
}

.preview-topbar span:nth-child(3) {
  background: #96dfb8;
}

.preview-hero {
  position: relative;
  min-height: 440px;
  overflow: hidden;
  color: #fff;
  background: #0b3a4f;
}

.preview-hero img,
.preview-hero video {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-empty {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  color: rgba(255, 255, 255, 0.46);
  font-size: 42px;
  background:
    linear-gradient(rgba(255, 255, 255, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.08) 1px, transparent 1px),
    #0b3a4f;
  background-size: 28px 28px;
}

.preview-overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(180deg, rgba(4, 24, 34, 0.22), rgba(4, 24, 34, 0.72)),
    rgba(0, 0, 0, 0.2);
}

.preview-nav-row {
  position: relative;
  z-index: 1;
  margin: 18px;
  padding: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 12px;
  font-weight: 800;
  background: rgba(255, 255, 255, 0.16);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

.preview-logo {
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  flex: 0 0 auto;
  color: #0f8fc3;
  background: #fff;
  border-radius: 999px;
}

.preview-logo img {
  position: static;
  width: 20px;
  height: 20px;
  object-fit: contain;
}

.preview-hero-copy {
  position: absolute;
  z-index: 1;
  left: 26px;
  right: 26px;
  bottom: 34px;
}

.preview-hero-copy small,
.section-preview-block small,
.submit-preview-card small {
  display: inline-flex;
  margin-bottom: 12px;
  padding: 7px 12px;
  color: #dffaff;
  font-size: 12px;
  font-weight: 900;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 999px;
}

.preview-hero-copy h3,
.section-preview-block h3,
.submit-preview-card h3 {
  margin: 0 0 10px;
  font-size: 28px;
  line-height: 1.18;
  font-weight: 900;
  letter-spacing: 0;
}

.preview-hero-copy p,
.section-preview-block p,
.submit-preview-card p,
.footer-preview-card p {
  margin: 0;
  color: rgba(255, 255, 255, 0.82);
  font-size: 14px;
  line-height: 1.7;
}

.preview-hero-copy div {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.preview-hero-copy button,
.preview-actions button {
  min-height: 36px;
  padding: 0 16px;
  color: #073748;
  font-weight: 900;
  background: #fff;
  border: 0;
  border-radius: 999px;
}

.preview-hero-copy button.ghost,
.preview-actions button + button {
  color: #fff;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.24);
}

.preview-stat-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.preview-stat-grid div {
  padding: 14px;
  display: grid;
  gap: 4px;
  background: #fff;
  border: 1px solid rgba(188, 223, 235, 0.82);
  border-radius: 8px;
}

.preview-stat-grid strong {
  color: #0f8fc3;
  font-size: 18px;
  line-height: 1.2;
  font-weight: 900;
}

.preview-stat-grid span {
  color: #6a808b;
  font-size: 12px;
}

.section-preview-block {
  padding: 24px;
}

.section-preview-block small {
  color: #0f8fc3;
  background: #e8f8fb;
  border-color: rgba(32, 184, 210, 0.2);
}

.section-preview-block h3 {
  color: #173f56;
}

.section-preview-block p {
  color: #5f788f;
}

.section-preview-block.dark {
  color: #fff;
  background: #0b3a4f;
  border-color: rgba(11, 58, 79, 0.4);
}

.section-preview-block.dark small {
  color: #dffaff;
  background: rgba(255, 255, 255, 0.12);
  border-color: rgba(255, 255, 255, 0.16);
}

.section-preview-block.dark h3 {
  color: #fff;
}

.mini-card-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: 18px;
}

.mini-card-grid div {
  min-width: 0;
  padding: 10px;
  background: rgba(235, 248, 252, 0.86);
  border-radius: 8px;
}

.dark .mini-card-grid div {
  background: rgba(255, 255, 255, 0.12);
}

.mini-card-grid span {
  display: block;
  aspect-ratio: 4 / 3;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #d9f7fc, #fff7ed);
  border-radius: 6px;
}

.dark .mini-card-grid span {
  background: linear-gradient(135deg, rgba(39, 196, 215, 0.42), rgba(255, 213, 138, 0.36));
}

.mini-card-grid strong {
  display: block;
  overflow: hidden;
  color: #173f56;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dark .mini-card-grid strong {
  color: #fff;
}

.sub-config-title {
  margin: 18px 0 12px;
  color: #173f56;
  font-size: 14px;
  font-weight: 900;
}

.topic-config-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.topic-editor {
  min-width: 0;
  padding: 14px;
  display: grid;
  gap: 10px;
  background: #f7fbfd;
  border: 1px solid rgba(202, 222, 230, 0.9);
  border-radius: 8px;
}

.topic-editor span {
  color: #0f8fc3;
  font-size: 12px;
  font-weight: 900;
}

.submit-preview-card {
  padding: 26px;
  color: #173f56;
  background: linear-gradient(135deg, #fff, #eefdff);
}

.submit-preview-card small {
  color: #c75a0e;
  background: #fff7ed;
  border-color: rgba(249, 115, 22, 0.18);
}

.submit-preview-card p {
  color: #5f788f;
}

.preview-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.preview-actions button {
  color: #fff;
  background: #f97316;
}

.preview-actions button + button {
  color: #0f6f84;
  background: #e8f8fb;
  border-color: rgba(32, 184, 210, 0.22);
}

.preview-step-row {
  display: grid;
  gap: 8px;
  margin-top: 18px;
}

.preview-step-row span {
  padding: 10px 12px;
  color: #173f56;
  font-size: 13px;
  font-weight: 800;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(202, 222, 230, 0.72);
  border-radius: 8px;
}

.footer-preview-card {
  padding: 22px;
  color: #fff;
  background: #092d3d;
}

.footer-preview-card strong {
  display: block;
  margin-bottom: 12px;
  font-size: 16px;
}

.footer-preview-card p {
  color: rgba(220, 233, 238, 0.84);
}

.footer-preview-card img {
  width: 82px;
  height: 82px;
  margin-top: 14px;
  object-fit: cover;
  background: #fff;
  border: 4px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
}

.content-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  background: linear-gradient(135deg, #fff, #f3fcff);
  border: 1px solid rgba(202, 222, 230, 0.9);
  border-radius: 8px;
}

:global(.table-media-thumb),
:global(.table-media-empty) {
  width: 72px;
  height: 52px;
  overflow: hidden;
  border: 1px solid #dce8ed;
  border-radius: 8px;
  background: #f5fbff;
}

:global(.table-media-empty) {
  display: grid;
  place-items: center;
  color: #8aa4b3;
  font-size: 22px;
}

:global(.dialog-field-grid) {
  display: grid;
  grid-template-columns: minmax(0, 180px) minmax(0, 1fr);
  gap: 16px;
}

:global(.content-media-control) {
  width: 100%;
}

:global(.content-media-control .media-field) {
  width: 100%;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

:global(.media-hint) {
  margin: 8px 0 0;
  color: #6a808b;
  font-size: 12px;
  line-height: 1.6;
}

:global(.media-preview-row) {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

:global(.media-preview-image) {
  width: 128px;
  height: 88px;
  overflow: hidden;
  border: 1px solid #dce8ed;
  border-radius: 8px;
  background: #f5fbff;
}

:global(.crop-editor) {
  display: grid;
  gap: 18px;
}

:global(.crop-stage) {
  position: relative;
  width: min(100%, 640px);
  margin: 0 auto;
  overflow: hidden;
  cursor: grab;
  user-select: none;
  border: 1px solid rgba(24, 185, 236, 0.32);
  border-radius: 8px;
  background:
    linear-gradient(45deg, rgba(21, 158, 229, 0.08) 25%, transparent 25%),
    linear-gradient(-45deg, rgba(21, 158, 229, 0.08) 25%, transparent 25%),
    linear-gradient(45deg, transparent 75%, rgba(21, 158, 229, 0.08) 75%),
    linear-gradient(-45deg, transparent 75%, rgba(21, 158, 229, 0.08) 75%),
    #f8fcff;
  background-position: 0 0, 0 10px, 10px -10px, -10px 0;
  background-size: 20px 20px;
}

:global(.crop-stage.is-dragging) {
  cursor: grabbing;
}

:global(.crop-stage img) {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  transform-origin: center;
  pointer-events: none;
}

:global(.crop-guide) {
  position: absolute;
  right: 12px;
  bottom: 12px;
  padding: 6px 10px;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  background: rgba(8, 47, 73, 0.68);
  border-radius: 999px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

:global(.crop-empty) {
  min-height: 260px;
  display: grid;
  place-items: center;
  gap: 8px;
  color: #6a808b;
}

:global(.crop-controls) {
  display: grid;
  gap: 14px;
}

:global(.crop-control-row),
:global(.crop-slider) {
  display: grid;
  grid-template-columns: 86px minmax(0, 1fr);
  align-items: center;
  gap: 14px;
  color: #315266;
  font-weight: 700;
}

:global(.crop-slider) {
  margin: 0;
}

@media (max-width: 1180px) {
  .settings-layout {
    grid-template-columns: 1fr;
  }

  .landing-preview {
    position: static;
  }

  .field-grid.four-cols {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
    padding: 22px;
  }

  .landing-tabs {
    padding: 16px;
  }

  .field-grid.two-cols,
  .field-grid.three-cols,
  .field-grid.four-cols,
  .topic-config-grid,
  .mini-card-grid,
  :global(.dialog-field-grid) {
    grid-template-columns: 1fr;
  }

  .media-field,
  :global(.content-media-control .media-field) {
    grid-template-columns: 1fr;
  }

  .media-meta,
  .content-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .media-preview,
  .media-preview.compact,
  .media-preview.qr {
    grid-template-columns: 1fr;
  }

  .preview-hero {
    min-height: 360px;
  }

  .preview-hero-copy h3 {
    font-size: 23px;
  }

  .preview-stat-grid {
    grid-template-columns: 1fr;
  }

  :global(.media-preview-row) {
    align-items: flex-start;
    flex-direction: column;
  }

  :global(.crop-control-row),
  :global(.crop-slider) {
    grid-template-columns: 1fr;
    gap: 8px;
  }
}
</style>

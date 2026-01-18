-- ComfyUI服务高级功能配置字段迁移脚本
-- 创建时间: 2026-01-18

-- 1. 添加高级功能配置字段
ALTER TABLE comfyui_server ADD COLUMN IF NOT EXISTS advanced_features_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE comfyui_server ADD COLUMN IF NOT EXISTS advanced_features JSONB;

-- 2. 添加字段注释
COMMENT ON COLUMN comfyui_server.advanced_features_enabled IS '是否启用高级功能';
COMMENT ON COLUMN comfyui_server.advanced_features IS '高级功能配置（JSON格式存储）';

-- 3. 创建索引
CREATE INDEX IF NOT EXISTS idx_comfyui_server_advanced_features_enabled ON comfyui_server(advanced_features_enabled);

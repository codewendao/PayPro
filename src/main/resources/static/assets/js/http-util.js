// HTTP请求工具类
class HttpUtil {
    constructor() {
        // 检查API_CONFIG是否已加载
        if (typeof API_CONFIG === 'undefined') {
            console.error('API_CONFIG未定义，请确保api-config.js已正确加载');
            throw new Error('API_CONFIG is not defined');
        }
        
        this.baseURL = API_CONFIG.BASE_URL;
        this.timeout = API_CONFIG.TIMEOUT;
        this.defaultHeaders = API_CONFIG.HEADERS;
    }

    // 通用请求方法
    async request(url, options = {}) {
        const config = {
            method: 'GET',
            headers: { ...this.defaultHeaders, ...options.headers },
            ...options
        };

        // 如果有请求体且不是FormData，转换为JSON字符串
        if (config.body && !(config.body instanceof FormData)) {
            config.body = JSON.stringify(config.body);
        }

        const fullUrl = url.startsWith('http') ? url : `${this.baseURL}${url}`;

        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), this.timeout);

            const response = await fetch(fullUrl, {
                ...config,
                signal: controller.signal
            });

            clearTimeout(timeoutId);

            if (!response.ok) {
                throw new Error(`HTTP Error: ${response.status} ${response.statusText}`);
            }

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            } else {
                return await response.text();
            }
        } catch (error) {
            if (error.name === 'AbortError') {
                throw new Error('请求超时，请检查网络连接');
            }
            throw error;
        }
    }

    // GET请求
    async get(url, params = {}) {
        const queryString = new URLSearchParams(params).toString();
        const fullUrl = queryString ? `${url}?${queryString}` : url;
        return this.request(fullUrl);
    }

    // POST请求
    async post(url, data = {}) {
        return this.request(url, {
            method: 'POST',
            body: data
        });
    }

    // PUT请求
    async put(url, data = {}) {
        return this.request(url, {
            method: 'PUT',
            body: data
        });
    }

    // DELETE请求
    async delete(url) {
        return this.request(url, {
            method: 'DELETE'
        });
    }

    // 上传文件
    async upload(url, formData) {
        return this.request(url, {
            method: 'POST',
            body: formData,
            headers: {} // 让浏览器自动设置Content-Type
        });
    }
}

// 全局单例实例
let httpUtil = null;

// 初始化函数 - 确保只初始化一次
function initHttpUtil() {
    // 检查是否已经初始化
    if (httpUtil) {
        return httpUtil;
    }
    
    // 检查API_CONFIG是否已加载
    if (typeof API_CONFIG === 'undefined') {
        console.error('API_CONFIG未定义，请确保api-config.js已正确加载');
        return null;
    }
    
    try {
        httpUtil = new HttpUtil();
        console.log('HttpUtil初始化成功（全局单例）');
    } catch (error) {
        console.error('HttpUtil初始化失败:', error);
        httpUtil = null;
    }
    return httpUtil;
}

// 获取httpUtil实例
function getHttpUtil() {
    return httpUtil || initHttpUtil();
}

// 简化的HTTP请求方法，自动处理初始化和错误
async function httpRequest(endpoint, options = {}) {
    try {
        // 确保httpUtil已初始化（单例模式）
        const util = getHttpUtil();
        
        if (!util) {
            throw new Error('HTTP工具初始化失败');
        }
        
        // 根据请求类型调用相应方法
        if (options.method === 'GET' || options.method === 'DELETE') {
            return await util.get(endpoint, options.params);
        } else {
            return await util.post(endpoint, options.data);
        }
    } catch (error) {
        console.error('HTTP请求失败:', error);
        throw error;
    }
}

// 便捷的POST请求方法
async function postRequest(endpoint, data = {}) {
    return httpRequest(endpoint, {
        method: 'POST',
        data: data
    });
}

// 便捷的GET请求方法
async function getRequest(endpoint, params = {}) {
    return httpRequest(endpoint, {
        method: 'GET',
        params: params
    });
}

// 导出工具类
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { HttpUtil, getHttpUtil, initHttpUtil, httpRequest, postRequest, getRequest };
} else {
    window.HttpUtil = HttpUtil;
    window.getHttpUtil = getHttpUtil;
    window.initHttpUtil = initHttpUtil;
    window.httpRequest = httpRequest;
    window.postRequest = postRequest;
    window.getRequest = getRequest;
    
    // 在DOM加载完成后初始化
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            httpUtil = initHttpUtil();
            window.httpUtil = httpUtil;
        });
    } else {
        httpUtil = initHttpUtil();
        window.httpUtil = httpUtil;
    }
}

// 确保在全局可用
if (typeof window !== 'undefined') {
    window.httpUtil = httpUtil;
}
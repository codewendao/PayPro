// API配置文件

window.API_CUSTOM_CONFIG = window.API_CUSTOM_CONFIG || {};

const API_CONFIG = {
    // 动态获取基础URL
    get BASE_URL() {
        // 1. 首先检查是否有全局配置
        if (window.API_CUSTOM_CONFIG && window.API_CUSTOM_CONFIG.BASE_URL) {
            return window.API_CUSTOM_CONFIG.BASE_URL;
        }

        // 2. 检查是否有URL参数配置
        if (typeof window !== 'undefined' && window.location) {
            const urlParams = new URLSearchParams(window.location.search);
            const apiBaseFromUrl = urlParams.get('apiBase');
            if (apiBaseFromUrl) {
                return apiBaseFromUrl;
            }
        }

        // 3. 检查是否有localStorage配置
        if (typeof localStorage !== 'undefined') {
            const storedApiBase = localStorage.getItem('API_BASE_URL');
            if (storedApiBase) {
                return storedApiBase;
            }
        }

        // 4. 都没有配置，则使用当前域名
        if (typeof window !== 'undefined' && window.location) {
            return window.location.origin; // 返回当前域名（包含协议和端口）
        }

        // 5. 最后兜底默认值
        return 'http://p796ebc2.natappfree.cc';
    },

    // API端点配置
    ENDPOINTS: {
        RECHARGE: '/order/add',
        STATUS: '/order/state',
        PRODUCT_DETAIL: '/product/get',
        PRODUCT_LIST: '/product/getListByType',
        PAY_LIST: '/order/list'
    },

    // 请求超时配置
    TIMEOUT: 10000,

    // 请求头配置
    HEADERS: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
};
// 导出配置
if (typeof module !== 'undefined' && module.exports) {
    module.exports = API_CONFIG;
} else {
    window.API_CONFIG = API_CONFIG;
}
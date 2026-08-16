export type PagedApiResponse<T> = {
  data: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  message: string;
  timestamp: string;
};

export type ApiResponse<T> = {
  data: T;
  message: string;
  timestamp: string;
};

export type ApiError = {
  timestamp?: string;
  status?: number;
  error?: string;
  message: string;
  path?: string;
  fieldErrors?: Array<{
    field: string;
    message: string;
  }>;
};

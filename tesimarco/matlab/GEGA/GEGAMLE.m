clc;
clear all;
close all;

if(matlabpool('size') == 0)
    maxNumCompThreads(2);
    matlabpool local 2;
end;

start=tic;
Ts=[0:1:128];
for T=Ts
    T
    if exist(['GMLE-T-' int2str(T) '.mat'],'file')~=2
        slots=20;
        GMLE=zeros(T+1,T+1,slots);
        b=2;
        p=1./(b.^(1:60));
        
        tic;
        if (T~=0)
            sficsT0='((1-p(l))^n)^i * (n*p(l)*(1-p(l))^(n-1))^s * (1-(1-p(l))^n-n*p(l)*(1-p(l))^(n-1))^c';
            parfor l=1:slots
                l
                sfG0=sfG(l);
                %sfG1=diff(sfG0,'n');
                %sficsT1=diff(sficsT0,'n');
                %sfG1=char(sfG1);
                %sficsT1=char(sficsT1);
                sfT0=[sficsT0 ' * '  sfG0];
                sfT0=strrep(sfT0,'*','.*');
                sfT0=strrep(sfT0,'^','.^');
                sfT0=strrep(sfT0,'/','./');
                fT0=inline(sfT0,'p','n','l','i','s','c');
                GMLEtmp=zeros(T+1,T+1);
                for c=0:T
                    for s=0:T-c
                        i=T-s-c;
                        GMLEtmp(c+1,s+1)=deasysolve(fT0,p,l,i,s,c);
                    end
                end
                GMLE(:,:,l)=GMLEtmp;
            end
        else
            parfor l=1:slots
                GMLE(:,:,l)=2^l;
            end
        end
        toc;
        
        save(['GMLE-T-' int2str(T)], 'GMLE','T','b','slots','p');
    end
end
stop=toc;